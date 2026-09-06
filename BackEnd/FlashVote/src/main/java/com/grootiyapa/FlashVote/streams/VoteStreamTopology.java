package com.grootiyapa.FlashVote.streams;


import com.grootiyapa.FlashVote.entity.VoteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
@EnableKafkaStreams
@RequiredArgsConstructor
@Slf4j
public class VoteStreamTopology {

    private final StringRedisTemplate redisTemplate;

    @Bean
    public KStream<String, VoteEvent> voteStream(StreamsBuilder builder) {

        // ── STEP 1: READ from votes.validated ──────────────────────────
        // builder.stream() creates a KStream — an infinite stream of events
        // think of it as a river of VoteEvent messages flowing continuously
        // Consumed.with() tells Kafka Streams how to deserialize the messages:
        //   - key is a String (pollId)
        //   - value is a VoteEvent (deserialize from JSON using JsonSerde)
        KStream<String, VoteEvent> stream = builder.stream(
                "votes.validated",
                Consumed.with(Serdes.String(), new JsonSerde<>(VoteEvent.class))
        );

        // ── STEP 2: REKEY each message ──────────────────────────────────
        // right now the key is just "poll_1"
        // we need to count per poll AND per option
        // so we change the key to "poll_1:option_A"
        // selectKey() transforms the key of every single message
        // (pollId, event) → the current key and value of each message
        KStream<String, VoteEvent> rekeyed = stream.selectKey(
                (pollId, event) ->
                        event.getPollId() + ":" + event.getOptionId()
        );

        // ── STEP 3: GROUP BY KEY ────────────────────────────────────────
        // groupByKey() tells Kafka Streams:
        // "collect all messages that share the same key together"
        // so all votes for "poll_1:option_A" are grouped
        // all votes for "poll_1:option_B" are grouped separately
        // Grouped.with() tells it how to serialize the grouped data
        KGroupedStream<String, VoteEvent> grouped = rekeyed.groupByKey(
                Grouped.with(Serdes.String(), new JsonSerde<>(VoteEvent.class))
        );

        // ── STEP 4: COUNT ───────────────────────────────────────────────
        // count() does the actual tallying
        // every time a new message arrives for "poll_1:option_A",
        // Kafka Streams increments its internal counter by 1
        // the result is a KTable — think of it as a live updating table:
        //   "poll_1:option_A" → 42
        //   "poll_1:option_B" → 17
        //   "poll_1:option_C" → 31
        // Materialized.as() gives the internal store a name
        // so you can query it later if needed (e.g. "what's the current count?")
        KTable<String, Long> voteCounts = grouped.count(
                Materialized.as("vote-counts-store")
        );


        // ── STEP 5: WRITE TO REDIS ──────────────────────────────────────
        // toStream() converts the KTable back to a stream
        // every time any count changes, it emits a new event
        // foreach() lets you run custom code for each updated count
        // key   = "poll_1:option_A"
        // count = 42 (the new total after this vote)
        voteCounts.toStream().foreach((key, count) -> {
            redisTemplate.opsForValue()
                    .set("votes:" + key, String.valueOf(count));

            log.info("Updated count in Redis --> votes:{} = {}", key, count);

            redisTemplate.convertAndSend(
                    "vote-updates",
                    key + "=" + count
            );
        });

        return stream;
    }
}

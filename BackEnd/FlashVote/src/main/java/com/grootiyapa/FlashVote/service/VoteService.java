package com.grootiyapa.FlashVote.service;

import com.grootiyapa.FlashVote.entity.Vote;
import com.grootiyapa.FlashVote.entity.VoteEvent;
import com.grootiyapa.FlashVote.entity.VoteRequest;
import com.grootiyapa.FlashVote.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;

    public void saveNewVote(Vote vote)
    {
        voteRepository.save(vote);
    }

//    @Autowired
//    private KafkaTemplate<String, Vote> kafkaTemplate;
//
//    public void sendVote(Vote event) {
//        kafkaTemplate.send("validated-votes", event);
//    }

    private final KafkaTemplate<String, VoteEvent> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;

    @Value("${topics.votes-raw}") private String rawTopic;
    @Value("${topics.votes-dlq}") private String dlqTopic;

    public void handleVote(VoteRequest request) {
        String voteId = UUID.randomUUID().toString();

        String dedupKey = "vote:" + request.getUserId() + ":" + request.getPollId();
        // We build a unique key per user per poll. For example:
        //vote:user_123:poll_1
        //This key will live in Redis. Think of Redis as a giant whiteboard that remembers things super fast.

        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(dedupKey, voteId, Duration.ofHours(24));
        // This is the deduplication check — the most critical line.
        //setIfAbsent means: "write this key on the whiteboard, BUT only if it's not already there."
        //
        //First vote from user_123 on poll_1 → key doesn't exist → writes it → returns true
        //Second vote from same user → key already exists → does nothing → returns false
        //
        //The Duration.ofHours(24) means the key auto-erases after 24 hours (so the whiteboard doesn't fill up forever).
        //This one line atomically prevents double voting. Atomic means it's a single operation — there's no gap where two requests could both think they're "first".

        VoteEvent event = new VoteEvent(
                voteId,
                request.getPollId(),
                request.getOptionId(),
                request.getUserId(),
                Instant.now()
        );

        if (Boolean.TRUE.equals(isNew)) {
            kafkaTemplate.send(rawTopic, request.getPollId(), event);
        } else {
            KafkaTemplate.send(dlqTopic, request.getPollId(), event);
        }
    }
}

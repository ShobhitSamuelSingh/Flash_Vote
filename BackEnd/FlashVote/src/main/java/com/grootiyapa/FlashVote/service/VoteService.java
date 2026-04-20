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

        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(dedupKey, voteId, Duration.ofHours(24));

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

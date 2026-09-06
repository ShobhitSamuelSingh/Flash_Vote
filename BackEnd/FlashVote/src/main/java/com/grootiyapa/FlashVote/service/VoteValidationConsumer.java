package com.grootiyapa.FlashVote.service;

import com.grootiyapa.FlashVote.entity.VoteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteValidationConsumer {

    private final KafkaTemplate<String, VoteEvent> kafkaTemplate;

    private static final Map<String, Set<String>> VALID_POLLS = Map.of(
            "A", new HashSet<>(Set.of("B", "C", "D")),
            "X", new HashSet<>(Set.of("Y", "Z"))
    );

    @KafkaListener(topics = "votes.raw", groupId = "vote-validation-group")
    public void validate(VoteEvent event) {
        log.info("Validating vote: {}", event);

        if (isValid(event)) {
            log.info("Vote valid -> promoting to votes.validated");
            kafkaTemplate.send("votes.validated", event.getPollId(), event);
        } else {
            log.warn("Vote invalid -> sending to votes.dlq");
            kafkaTemplate.send("votes.dlq", event.getPollId(), event);
        }
    }

    private boolean isValid(VoteEvent event) {
        Set<String> validOptions = VALID_POLLS.get(event.getPollId());
        return validOptions != null && validOptions.contains(event.getOptionId());
    }



}

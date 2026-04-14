package com.grootiyapa.DBCleaner.service;

import com.grootiyapa.DBCleaner.entity.Vote;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class VoteConsumer {

    @KafkaListener(topics = "validated-votes", groupId = "vote-group")
    public void consume(Vote event) {
        System.out.println("Received vote from: " + event.getId());
    }
}
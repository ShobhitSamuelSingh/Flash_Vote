package com.grootiyapa.FlashVote.service;

import com.grootiyapa.FlashVote.entity.Vote;
import com.grootiyapa.FlashVote.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;

    public void saveNewVote(Vote vote)
    {
        voteRepository.save(vote);
    }

    @Autowired
    private KafkaTemplate<String, Vote> kafkaTemplate;

    public void sendVote(Vote event) {
        kafkaTemplate.send("validated-votes", event);
    }
}

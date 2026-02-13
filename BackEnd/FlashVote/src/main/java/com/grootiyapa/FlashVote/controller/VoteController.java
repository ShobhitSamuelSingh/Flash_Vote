package com.grootiyapa.FlashVote.controller;


import com.grootiyapa.FlashVote.entity.Vote;
import com.grootiyapa.FlashVote.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vote")
public class VoteController {

    @Autowired
    private VoteService voteService;

    @PostMapping
    public void createVote(@RequestBody Vote vote) {
        voteService.saveNewVote(vote);
    }

}

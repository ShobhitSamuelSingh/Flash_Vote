package com.grootiyapa.FlashVote.controller;


import com.grootiyapa.FlashVote.entity.Vote;
import com.grootiyapa.FlashVote.entity.VoteRequest;
import com.grootiyapa.FlashVote.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    @Autowired
    private VoteService voteService;

//    @PostMapping
//    public void createVote(@RequestBody Vote vote) {
//        voteService.saveNewVote(vote);
//    }

    @PostMapping
    public ResponseEntity<String> vote(@Valid @RequestBody VoteRequest request) {
        voteService.handleVote(request);
        return ResponseEntity.accepted().body("Vote received.");
    }
}

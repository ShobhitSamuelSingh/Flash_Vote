package com.grootiyapa.FlashVote.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class VoteEvent {
    private String voteId;
    private String pollId;
    private String optionId;
    private String userId;
    private Instant timestamp;
}

package com.grootiyapa.FlashVote.entity;

import java.time.Instant;

public class VoteEvent {
    private String voteId;
    private String pollId;
    private String optionId;
    private String userId;
    private Instant timestamp;
}

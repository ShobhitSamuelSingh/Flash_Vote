package com.grootiyapa.FlashVote.entity;

import jakarta.validation.constraints.NotBlank;

public class VoteRequest {
    @NotBlank private String pollId;
    @NotBlank private String optionId;
    @NotBlank private String userId;
}

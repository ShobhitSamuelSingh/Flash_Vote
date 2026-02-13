package com.grootiyapa.FlashVote.repository;

import com.grootiyapa.FlashVote.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
}

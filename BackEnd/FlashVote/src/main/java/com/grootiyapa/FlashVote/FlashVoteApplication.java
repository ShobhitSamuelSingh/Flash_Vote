package com.grootiyapa.FlashVote;

import com.grootiyapa.FlashVote.entity.Vote;
import com.grootiyapa.FlashVote.service.VoteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FlashVoteApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashVoteApplication.class, args);
	}

//	@Bean
//	public CommandLineRunner run(VoteService voteService) {
//		return args -> {
//			voteService.sendVote(new Vote(123L, "candidateA"));
//		};
//	}

}

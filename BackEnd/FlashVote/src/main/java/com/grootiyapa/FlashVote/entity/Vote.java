package com.grootiyapa.FlashVote.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Vote")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAMe")
    private String name;


}

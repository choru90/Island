package com.choru.island.model.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String name;

    @Column(length = 500)
    private String address;

}
package com.b101.pickTime.db.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer songId;
}

package com.b101.pickTime.db.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "chords")
public class Chord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer chordId;

}

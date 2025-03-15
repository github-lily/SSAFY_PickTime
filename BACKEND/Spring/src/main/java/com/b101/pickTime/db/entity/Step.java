package com.b101.pickTime.db.entity;

import com.mongodb.lang.Nullable;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "steps")
public class Step {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stepId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name =  "stage_id", nullable = false)
    private Stage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY)
    private Chord chord;

    @Column(nullable = false)
    private Integer stepType;

    @Column(nullable = false)
    private Integer stepNumber;

    private String description;

}

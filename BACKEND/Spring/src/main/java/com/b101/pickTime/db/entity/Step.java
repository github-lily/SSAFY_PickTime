package com.b101.pickTime.db.entity;

import com.mongodb.lang.Nullable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Step {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stepId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name =  "stage_id", nullable = false)
    private Stage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name =  "song_id")
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name =  "chord_id")
    private Chord chord;

    @Column(nullable = false)
    private Integer stepType;

    @Column(nullable = false)
    private Integer stepNumber;

    private String description;

}

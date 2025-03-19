package com.b101.pickTime.db.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "completed_songs")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletedSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer completedSongId;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name="song_id", nullable = false)
    private Song song;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Integer score;

}

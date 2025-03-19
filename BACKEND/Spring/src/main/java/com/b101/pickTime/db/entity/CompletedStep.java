package com.b101.pickTime.db.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "completed_steps")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletedStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer completedStepId;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name="step_id", nullable = false)
    private Step step;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Integer score;
}

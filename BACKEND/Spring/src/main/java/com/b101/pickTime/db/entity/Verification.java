package com.b101.pickTime.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "verifications")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Verification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer verificationId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String verificationNumber;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    public void updateVerificationNumber(String verificationNumber) {
        this.verificationNumber = verificationNumber;
    }
    public void updateExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }
}

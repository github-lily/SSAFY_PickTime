package com.b101.pickTime.db.repository;

import com.b101.pickTime.db.entity.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Integer> {
    Verification findByUsername(String username);
}

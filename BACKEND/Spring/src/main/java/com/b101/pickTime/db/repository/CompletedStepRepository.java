package com.b101.pickTime.db.repository;

import com.b101.pickTime.db.entity.CompletedStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletedStepRepository extends JpaRepository<CompletedStep, Integer> {
}

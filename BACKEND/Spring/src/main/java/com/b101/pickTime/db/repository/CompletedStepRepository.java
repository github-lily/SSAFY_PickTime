package com.b101.pickTime.db.repository;

import com.b101.pickTime.db.entity.CompletedStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompletedStepRepository extends JpaRepository<CompletedStep, Integer> {

    @Query("SELECT COUNT(cs) FROM CompletedStep cs WHERE cs.user.userId = :userId")
    long countByUserId(@Param("userId") Integer userId);
}

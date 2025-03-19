package com.b101.pickTime.db.repository;

import com.b101.pickTime.api.completedactivities.response.CompletedActivitiesResDto;
import com.b101.pickTime.db.entity.CompletedStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompletedStepRepository extends JpaRepository<CompletedStep, Integer> {

    @Query("SELECT COUNT(cs) FROM CompletedStep cs WHERE cs.user.userId = :userId")
    long countByUserId(@Param("userId") Integer userId);

    @Query("SELECT new com.b101.pickTime.api.completedactivities.response.CompletedActivitiesResDto(" +
            "c.createdAt, COUNT(c)) " +
            "FROM CompletedStep c " +
            "WHERE c.user.userId = :userId " +
            "GROUP BY c.createdAt")
    List<CompletedActivitiesResDto> countCompletedStepsGroupedByCreatedAt(@Param("userId") Integer userId);
}

package com.b101.pickTime.db.repository;

import com.b101.pickTime.api.step.response.StepResDto;
import com.b101.pickTime.db.entity.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StepRepository extends JpaRepository<Step, Integer> {
    @Query("SELECT new com.b101.pickTime.api.step.response.StepResDto(" +
            "s.stepId, s.description, s.stepNumber ," +
            "CASE WHEN c.completedStepId IS NOT NULL THEN true ELSE false END) " +
            "FROM Step s LEFT JOIN CompletedStep c ON s.stepId = c.step.stepId AND c.user.userId = :userId " +
            "WHERE s.stage.stageId = :stageId " +
            "ORDER BY s.stepNumber ASC")
    public List<StepResDto> findStepsWithClearStatus(@Param("stageId") Integer stageId,
                                                     @Param("userId") Integer userId);

}

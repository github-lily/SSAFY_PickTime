package com.b101.pickTime.db.repository;

import com.b101.pickTime.api.step.response.StepInfoResDto;
import com.b101.pickTime.api.step.response.StepResDto;
import com.b101.pickTime.db.entity.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StepRepository extends JpaRepository<Step, Integer> {
    @Query("SELECT new com.b101.pickTime.api.step.response.StepResDto(" +
            "  s.stepId, " +
            "  s.description, " +
            "  s.stepNumber, " +
            "  CASE WHEN COUNT(c.completedStepId) > 0 THEN true ELSE false END, " +         // 하나라도 있으면 true
            "  CASE WHEN COUNT(c.completedStepId) > 0 THEN COALESCE(MAX(c.score), 0) ELSE 0 END " +
            ") " +
            "FROM Step s " +
            "LEFT JOIN CompletedStep c ON s.stepId = c.step.stepId AND c.user.userId = :userId " +
            "WHERE s.stage.stageId = :stageId " +
            "GROUP BY s.stepId,  s.stepNumber " +  // Step별로 묶기
            "ORDER BY s.stepNumber ASC")
    List<StepResDto> findStepsWithClearStatus(@Param("stageId") Integer stageId,
                                              @Param("userId") Integer userId);


    @Query(value = "SELECT s.step_type AS stepType, " +
            "       s.chord_id AS chordId, " +
            "       s.song_id AS songId, " +
            "       s.stage_id AS stageId, " +
            "       s.description " +
            "FROM steps s " +
            "WHERE s.step_id = :stepId",
            nativeQuery = true)
    StepInfoResDto findStepInfoByStepId(@Param("stepId") Integer stepId);

    @Query("SELECT DISTINCT s.chord.chordId " +
            "FROM Step s " +
            "WHERE s.stage.stageId = :stageId " +
            "AND s.chord IS NOT NULL")
    List<Integer> findChordIdByStageId(@Param("stageId") Integer stageId);

}

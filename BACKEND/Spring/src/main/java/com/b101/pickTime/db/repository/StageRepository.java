package com.b101.pickTime.db.repository;

import com.b101.pickTime.api.stage.response.StageResDto;
import com.b101.pickTime.db.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StageRepository extends JpaRepository<Stage, Integer> {

    @Query("SELECT new com.b101.pickTime.api.stage.response.StageResDto(" +
            "s.stageId, s.description)" +
            "FROM Stage s")
    List<StageResDto> findAllStage();

    
}

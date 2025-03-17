package com.b101.pickTime.api.practice.service.impl;

import com.b101.pickTime.api.practice.response.CurriculumResDto;
import com.b101.pickTime.api.practice.service.PracticeApplicationService;
import com.b101.pickTime.api.stage.response.StageResDto;
import com.b101.pickTime.api.stage.service.StageService;
import com.b101.pickTime.api.step.response.StepResDto;
import com.b101.pickTime.api.step.servce.StepService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PracticeApplicationServiceImpl implements PracticeApplicationService {

    private final StageService stageService;
    private final StepService stepService;
    @Override
    public CurriculumResDto getCurriculum(Integer userId){
        List<StageResDto> stages = stageService.getStages();
        int numberOfClearStages = stages.size();
        for(StageResDto stage : stages){
            List<StepResDto> steps = stepService.getSteps(stage.getStageId(), userId);
            stage.setSteps(steps);
            boolean isClear = true;
            for(StepResDto step : steps){
                if(!step.getIsClear()){
                    isClear = false;
                    numberOfClearStages--;
                    break;
                }
            }

            stage.setIsClear(isClear);     // 추후 수정
        }

        return new CurriculumResDto(stages, (double)numberOfClearStages/stages.size());
    }

}

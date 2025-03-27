package com.b101.pickTime.api.step.servce;

import com.b101.pickTime.api.step.response.StepInfoResDto;
import com.b101.pickTime.api.step.response.StepResDto;

import java.util.List;

public interface StepService {

    List<StepResDto> getSteps(int stageId, int userId);
    StepInfoResDto getStepInfo(int stepId);

    List<Integer> getChordsFromStage(int stageId);
}

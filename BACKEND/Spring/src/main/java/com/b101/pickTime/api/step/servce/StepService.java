package com.b101.pickTime.api.step.servce;

import com.b101.pickTime.api.step.response.StepResDto;

import java.util.List;

public interface StepService {

    public List<StepResDto> getSteps(int stageId, int userId);

}

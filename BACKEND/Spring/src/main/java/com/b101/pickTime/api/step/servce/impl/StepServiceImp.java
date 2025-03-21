package com.b101.pickTime.api.step.servce.impl;

import com.b101.pickTime.api.step.response.StepResDto;
import com.b101.pickTime.api.step.servce.StepService;
import com.b101.pickTime.db.repository.StepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StepServiceImp implements StepService {

    private final StepRepository stepRepository;

    @Override
    public List<StepResDto> getSteps(int stageId, int userId) {
        return stepRepository.findStepsWithClearStatus(stageId, userId);
    }
}

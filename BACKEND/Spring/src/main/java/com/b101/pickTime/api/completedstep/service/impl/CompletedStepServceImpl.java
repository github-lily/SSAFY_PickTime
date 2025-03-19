package com.b101.pickTime.api.completedstep.service.impl;

import com.b101.pickTime.api.completedactivities.response.CompletedActivitiesResDto;
import com.b101.pickTime.api.completedstep.service.CompletedStepService;
import com.b101.pickTime.db.entity.CompletedStep;
import com.b101.pickTime.db.entity.Step;
import com.b101.pickTime.db.entity.User;
import com.b101.pickTime.db.repository.CompletedStepRepository;
import com.b101.pickTime.db.repository.StepRepository;
import com.b101.pickTime.db.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompletedStepServceImpl implements CompletedStepService {

    private final CompletedStepRepository completedStepRepository;
    private final UserRepository userRepository;
    private final StepRepository stepRepository;

    @Override
    public void completeStep(Integer userId, Integer stepId, Integer score) {

        User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("찾는 회원이 없습니다."));

        Step step = stepRepository.findById(stepId)
                    .orElseThrow(() -> new EntityNotFoundException("찾는 스텝이 없습니다."));

        completedStepRepository.save(
                    CompletedStep.builder()
                                .user(user)
                                .step(step)
                                .score(score)
                                .build()
        );
    }

    @Override
    public double getProgress(Integer userId) {
        return (double)completedStepRepository.countByUserId(userId)/ stepRepository.count();
    }

    @Override
    public List<CompletedActivitiesResDto> getPickDaysOfStep(Integer userId) {
        return completedStepRepository.countCompletedStepsGroupedByCreatedAt(userId);
    }
}

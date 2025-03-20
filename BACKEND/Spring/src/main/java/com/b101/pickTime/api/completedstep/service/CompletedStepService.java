package com.b101.pickTime.api.completedstep.service;

import com.b101.pickTime.api.completedactivities.response.CompletedActivitiesResDto;

import java.util.List;

public interface CompletedStepService {

    public void completeStep(Integer userId, Integer stepId, Integer score);
    public double getProgress(Integer userId);
    public List<CompletedActivitiesResDto> getPickDaysOfStep(Integer userId);

}

package com.b101.pickTime.api.completedstep.service;

public interface CompletedStepService {

    public void completeStep(Integer userId, Integer stepId, Integer score);
    public double getProgress(Integer userId);

}

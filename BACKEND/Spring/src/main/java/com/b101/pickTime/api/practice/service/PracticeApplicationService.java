package com.b101.pickTime.api.practice.service;

import com.b101.pickTime.api.practice.response.CurriculumResDto;

public interface PracticeApplicationService {

    public CurriculumResDto getCurriculum(Integer userId);
    public void completeStep(Integer userId, Integer stepId, Integer score);

}

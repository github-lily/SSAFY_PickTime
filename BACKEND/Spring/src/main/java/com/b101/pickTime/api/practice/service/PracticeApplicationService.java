package com.b101.pickTime.api.practice.service;

import com.b101.pickTime.api.practice.response.CurriculumResDto;
import com.b101.pickTime.api.practice.response.PracticeResDto;

public interface PracticeApplicationService {

    CurriculumResDto getCurriculum(Integer userId);
    PracticeResDto getStep(Integer stepId);
}

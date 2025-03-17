package com.b101.pickTime.api.practice.controller;

import com.b101.pickTime.api.practice.response.CurriculumResDto;
import com.b101.pickTime.api.practice.service.PracticeApplicationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/practice")
@AllArgsConstructor
public class PracticeController {

    private final PracticeApplicationService practiceApplicationService;

    @GetMapping
    public ResponseEntity<CurriculumResDto> getCurriculum(Integer userId){
        CurriculumResDto curriculum = practiceApplicationService.getCurriculum(userId);
        return ResponseEntity.ok(curriculum);
    }

}

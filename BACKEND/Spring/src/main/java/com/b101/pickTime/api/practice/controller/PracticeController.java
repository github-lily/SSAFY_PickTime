package com.b101.pickTime.api.practice.controller;

import com.b101.pickTime.api.practice.response.CurriculumResDto;
import com.b101.pickTime.api.practice.response.PracticeResDto;
import com.b101.pickTime.api.practice.service.PracticeApplicationService;
import com.b101.pickTime.common.auth.CustomUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
@AllArgsConstructor
public class PracticeController {

    private final PracticeApplicationService practiceApplicationService;

    @GetMapping
    public ResponseEntity<CurriculumResDto> getCurriculum(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        CurriculumResDto curriculum = practiceApplicationService.getCurriculum(customUserDetails.getUserId());
        return ResponseEntity.ok(curriculum);

    }

    @GetMapping("/{stepId}")
    public ResponseEntity<PracticeResDto> getPractice(@PathVariable("stepId") Integer stepId){
        PracticeResDto practice = practiceApplicationService.getStep(stepId);
        return ResponseEntity.ok(practice);
    }
}

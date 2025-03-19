package com.b101.pickTime.api.practice.controller;

import com.b101.pickTime.api.ApiResponseDto;
import com.b101.pickTime.api.practice.request.CompleteStepReqDto;
import com.b101.pickTime.api.practice.response.CurriculumResDto;
import com.b101.pickTime.api.practice.service.PracticeApplicationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
@AllArgsConstructor
public class PracticeController {

    private final PracticeApplicationService practiceApplicationService;

    /*
        로그인 로직 추가 후 userId는 전부 @AuthenticationPrincipal로 교체
    */
    @GetMapping("/{userId}")
    public ResponseEntity<CurriculumResDto> getCurriculum(@PathVariable("userId") Integer userId){
        CurriculumResDto curriculum = practiceApplicationService.getCurriculum(userId);

        return ResponseEntity.ok(curriculum);

    }

    @PostMapping("/{stepId}")
    public ResponseEntity<String> completeStep(@PathVariable("stepId") Integer stepId,
                                                          @RequestBody CompleteStepReqDto completeStepReqDto){

        practiceApplicationService.completeStep(completeStepReqDto.getUserId(), stepId, completeStepReqDto.getScore());

        return ResponseEntity.ok("step 로직 완료");

    }

    @GetMapping("/{userId}/progress")
    public ResponseEntity<Double> getProgress(@PathVariable("userId") Integer userId){
        Double progress = practiceApplicationService.getProgress(userId);

        return ResponseEntity.ok(progress);

    }
}

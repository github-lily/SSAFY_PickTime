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
    public ResponseEntity<ApiResponseDto<CurriculumResDto>> getCurriculum(@PathVariable("userId") Integer userId){
        CurriculumResDto curriculum = practiceApplicationService.getCurriculum(userId);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "전체 커리큘럼 조회 완료",
                curriculum
        ));

    }

    @PostMapping("/{stepId}")
    public ResponseEntity<ApiResponseDto<?>> completeStep(@PathVariable("stepId") Integer stepId,
                                                          @RequestBody CompleteStepReqDto completeStepReqDto){

        practiceApplicationService.completeStep(completeStepReqDto.getUserId(), stepId, completeStepReqDto.getScore());

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "스텝 완료",
                null
        ));

    }

    @GetMapping("/{userId}/progress")
    public ResponseEntity<ApiResponseDto<Double>> getProgress(@PathVariable("userId") Integer userId){
        Double progress = practiceApplicationService.getProgress(userId);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "전체 진행률 조회 완료",
                progress
        ));

    }
}

package com.b101.pickTime.api.practice.controller;

import com.b101.pickTime.api.ApiResponseDto;
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

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseDto< CurriculumResDto>> getCurriculum(@PathVariable("userId") Integer userId){
        CurriculumResDto curriculum = practiceApplicationService.getCurriculum(userId);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "전체 커리큘럼 조회 완료",
                curriculum
        ));

    }

}

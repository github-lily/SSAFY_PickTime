package com.b101.pickTime.api.practice.controller;

import com.b101.pickTime.api.practice.response.CurriculumResDto;
import com.b101.pickTime.api.practice.service.PracticeApplicationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}

package com.b101.pickTime.api.completedstep.controller;

import com.b101.pickTime.api.completedstep.service.CompletedStepService;
import com.b101.pickTime.api.practice.request.CompleteStepReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/completed-step")
@RequiredArgsConstructor
public class CompletedStepControler {

    private final CompletedStepService completedStepService;

    /*
        로그인 로직 추가 후 userId는 전부 @AuthenticationPrincipal로 교체
    */

    @PostMapping("/{stepId}")
    public ResponseEntity<String> completeStep(@PathVariable("stepId") Integer stepId,
                                               @RequestBody CompleteStepReqDto completeStepReqDto){

        completedStepService.completeStep(completeStepReqDto.getUserId(), stepId, completeStepReqDto.getScore());

        return ResponseEntity.ok("step 로직 완료!");

    }

    @GetMapping("/{userId}/progress")
    public ResponseEntity<Double> getProgress(@PathVariable("userId") Integer userId){
        Double progress = completedStepService.getProgress(userId);

        return ResponseEntity.ok(progress);

    }

}

package com.b101.pickTime.api.completedstep.controller;

import com.b101.pickTime.api.completedstep.service.CompletedStepService;
import com.b101.pickTime.api.completedstep.request.CompleteStepReqDto;
import com.b101.pickTime.common.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
                                               @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                               @RequestBody CompleteStepReqDto completeStepReqDto){

        completedStepService.completeStep(customUserDetails.getUserId(), stepId, completeStepReqDto.getScore());

        return ResponseEntity.ok("step 로직 완료!");

    }

    @GetMapping("/progress")
    public ResponseEntity<Double> getProgress(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        Double progress = completedStepService.getProgress(customUserDetails.getUserId());

        return ResponseEntity.ok(progress);

    }

}

package com.b101.pickTime.api.completedactivities.controller;

import com.b101.pickTime.api.completedactivities.response.PickDaysResDto;
import com.b101.pickTime.api.completedactivities.service.CompletedActivitiesApplicationService;
import com.b101.pickTime.common.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/completed-activities")
@RequiredArgsConstructor
public class CompletedActivitiesController {

    private final CompletedActivitiesApplicationService completedActivitiesApplicationService;

    @GetMapping
    public ResponseEntity<PickDaysResDto> getPickDays(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        PickDaysResDto pickDays = completedActivitiesApplicationService.getPickDays(customUserDetails.getUserId());
        return ResponseEntity.ok(pickDays);
    }

}

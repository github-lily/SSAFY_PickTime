package com.b101.pickTime.api.completedactivities.controller;

import com.b101.pickTime.api.completedactivities.response.PickDaysResDto;
import com.b101.pickTime.api.completedactivities.service.CompletedActivitiesApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/completed-activities")
@RequiredArgsConstructor
public class CompletedActivitiesController {

    private final CompletedActivitiesApplicationService completedActivitiesApplicationService;

    @GetMapping("/{userId}")
    public ResponseEntity<PickDaysResDto> getPickDays(@PathVariable("userId") Integer userId){
        PickDaysResDto pickDays = completedActivitiesApplicationService.getPickDays(userId);
        return ResponseEntity.ok(pickDays);
    }

}

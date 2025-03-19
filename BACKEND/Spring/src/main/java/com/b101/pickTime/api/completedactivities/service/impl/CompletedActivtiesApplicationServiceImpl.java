package com.b101.pickTime.api.completedactivities.service.impl;


import com.b101.pickTime.api.completedactivities.response.CompletedActivitiesResDto;
import com.b101.pickTime.api.completedactivities.response.PickDaysResDto;
import com.b101.pickTime.api.completedactivities.service.CompletedActivitiesApplicationService;
import com.b101.pickTime.api.completedsong.service.CompletedSongService;
import com.b101.pickTime.api.completedstep.service.CompletedStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompletedActivtiesApplicationServiceImpl implements CompletedActivitiesApplicationService {

    private final CompletedStepService completedStepService;
    private final CompletedSongService completedSongService;

    @Override
    public PickDaysResDto getPickDays(Integer userId) {

        List<CompletedActivitiesResDto> pickdaysOfstep = completedStepService.getPickDaysOfStep(userId);
        List<CompletedActivitiesResDto> pickdaysOfsong = completedSongService.getPickDaysOfSong(userId);

        List<CompletedActivitiesResDto> pickdays = concatPickDays(pickdaysOfstep, pickdaysOfsong);
        int continuedPickDay = getContinuedPickDay(pickdays);

        return new PickDaysResDto(pickdays, continuedPickDay);
    }

    private List<CompletedActivitiesResDto> concatPickDays(List<CompletedActivitiesResDto> pickdaysOfstep, List<CompletedActivitiesResDto> pickdaysOfsong) {

        Map<LocalDate, Long> pickdays = new HashMap<>();

        for(CompletedActivitiesResDto pickdayOfstep : pickdaysOfstep){
            pickdays.merge(pickdayOfstep.getCompletedDate(), pickdayOfstep.getPickCount(), Long::sum);
        }

        for(CompletedActivitiesResDto pickdayOfsong : pickdaysOfsong){
            pickdays.merge(pickdayOfsong.getCompletedDate(), pickdayOfsong.getPickCount(), Long::sum);
        }

        return pickdays.entrySet().stream()
                .map(entry -> new CompletedActivitiesResDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private int getContinuedPickDay(List<CompletedActivitiesResDto> pickdays) {
        LocalDate today = LocalDate.now();
        int continuedDays = 0;

        pickdays.sort((o1, o2) -> -o1.getCompletedDate().compareTo(o2.getCompletedDate()));

        for (CompletedActivitiesResDto pickday : pickdays) {

            if (pickday.getCompletedDate().equals(today.minusDays(continuedDays))) {
                continuedDays++;
            } else {
                break;
            }
        }
        return continuedDays;
    }

}

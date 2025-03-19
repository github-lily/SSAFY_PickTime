package com.b101.pickTime.api.completedactivities.service;

import com.b101.pickTime.api.completedactivities.response.PickDaysResDto;

public interface CompletedActivitiesApplicationService {

    public PickDaysResDto getPickDays(Integer userId);

}

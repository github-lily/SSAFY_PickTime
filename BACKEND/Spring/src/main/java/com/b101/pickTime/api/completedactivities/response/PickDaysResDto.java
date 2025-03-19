package com.b101.pickTime.api.completedactivities.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class PickDaysResDto {

    private List<CompletedActivitiesResDto> pickDays;
    private int continued;

}

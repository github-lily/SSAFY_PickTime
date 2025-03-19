package com.b101.pickTime.api.completedactivities.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompletedActivitiesResDto {

    private LocalDate completedDate;
    private Long pickCount;

    public CompletedActivitiesResDto(LocalDateTime localDateTime, Long pickCount){
        this.completedDate = localDateTime.toLocalDate();
        this.pickCount = pickCount;
    }

}

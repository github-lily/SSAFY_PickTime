package com.b101.pickTime.api.practice.response;

import com.b101.pickTime.api.stage.response.StageResDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumResDto {

    private List<StageResDto> stages;
    private double clearRate;
}

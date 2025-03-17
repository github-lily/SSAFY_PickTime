package com.b101.pickTime.api.stage.response;

import com.b101.pickTime.api.step.response.StepResDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class StageResDto {

    private Integer stageId;
    private String stageDescription;
    private List<StepResDto> steps;
    private Boolean isClear;

    public StageResDto(Integer stageId, String stageDescription){
        this.stageId = stageId;
        this.stageDescription = stageDescription;
    }

}

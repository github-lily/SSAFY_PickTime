package com.b101.pickTime.api.step.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepInfoResDto {

    private Integer stepType;
    private Integer chordId;
    private Integer songId;
    private Integer stageId;
    private String description;

}

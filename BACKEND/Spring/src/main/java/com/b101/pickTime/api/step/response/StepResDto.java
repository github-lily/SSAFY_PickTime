package com.b101.pickTime.api.step.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StepResDto {

    private Integer stepId;
    private String stepDescription;
    private Integer stepNumber;
    private Boolean isClear;
    private Integer star;

}

package com.b101.pickTime.api.user.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CheckVerificationReq {
    private String username;
    private String verificationNumber;
}

package com.b101.pickTime.api.user.service;

import com.b101.pickTime.api.user.request.CheckVerificationReq;

public interface VerificationService {

    String getVerificationNumber();
    void createVerification(String username, String verificationNumber);
    boolean checkVerificationNumber(CheckVerificationReq checkVerificationReq);

}

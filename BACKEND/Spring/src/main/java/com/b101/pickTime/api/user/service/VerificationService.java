package com.b101.pickTime.api.user.service;

import com.b101.pickTime.api.user.request.CheckVerificationReq;
import com.b101.pickTime.api.user.request.EmailVerificationReq;

public interface VerificationService {
    void sendVerificationEmail(EmailVerificationReq emailVerificationReq);
    boolean checkVerificationNumber(CheckVerificationReq checkVerificationReq);

}

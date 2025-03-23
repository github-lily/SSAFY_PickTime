package com.b101.pickTime.api.user.service;

import com.b101.pickTime.api.user.request.EmailVerificationReq;


public interface UserApplicationService {

    void sendVerificationEmail(EmailVerificationReq emailVerificationReq);
}

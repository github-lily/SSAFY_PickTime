package com.b101.pickTime.api.user.service;

import com.b101.pickTime.api.user.request.CheckVerificationReq;
import com.b101.pickTime.api.user.request.EmailVerificationReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


public interface UserApplicationService {

    void sendVerificationEmail(EmailVerificationReq emailVerificationReq);
}

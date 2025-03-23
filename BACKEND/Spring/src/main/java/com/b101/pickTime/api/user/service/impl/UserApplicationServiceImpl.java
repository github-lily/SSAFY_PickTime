package com.b101.pickTime.api.user.service.impl;

import com.b101.pickTime.api.user.request.EmailVerificationReq;
import com.b101.pickTime.api.user.service.UserApplicationService;
import com.b101.pickTime.api.user.service.UserService;
import com.b101.pickTime.api.user.service.VerificationService;
import com.b101.pickTime.common.email.EmailSender;
import com.b101.pickTime.common.exception.exception.NotFoundUsernameException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserApplicationServiceImpl implements UserApplicationService {
    private final UserService userService;
    private final VerificationService verificationService;
    private final EmailSender emailSender;

    // 인증메일 전송
    public void sendVerificationEmail(EmailVerificationReq emailVerificationReq) {
        String username = emailVerificationReq.getUsername();

        // 해당 이메일 존재하는지 확인
        if (!userService.isExistUsername(username)) {
            throw new NotFoundUsernameException();
        }
        // 랜덤 인증번호 생성
        String verificationNumber = verificationService.getVerificationNumber();
        // DB에 인증정보 저장
        verificationService.createVerification(username, verificationNumber);
        // 이메일 전송
        emailSender.sendVerificationMail(username, verificationNumber);
    }

}

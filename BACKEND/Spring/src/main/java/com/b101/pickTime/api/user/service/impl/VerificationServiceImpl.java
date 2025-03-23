package com.b101.pickTime.api.user.service.impl;

import com.b101.pickTime.api.user.request.CheckVerificationReq;
import com.b101.pickTime.api.user.request.EmailVerificationReq;
import com.b101.pickTime.api.user.service.VerificationService;
import com.b101.pickTime.db.entity.Verification;
import com.b101.pickTime.db.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {
    private final int EXPIRATION_SECONDS = 60*3; // 유효시간 3분
    
    private final VerificationRepository verificationRepository;

    public void createVerification(String username, String verificationNumber) {
        // 해당 이메일로 보낸 인증번호 내역 존재하는지 확인
        Verification verification = verificationRepository.findByUsername(username);
        if (verification == null) {
            verificationRepository.save(Verification.builder()
                    .username(username)
                    .verificationNumber(verificationNumber)
                    .expirationTime(LocalDateTime.now().plusSeconds(EXPIRATION_SECONDS))
                    .build());
            return;
        }
        // 인증번호 수정하여 저장
        verification.updateVerificationNumber(verificationNumber);
        verification.updateExpirationTime(LocalDateTime.now().plusSeconds(EXPIRATION_SECONDS));
        verificationRepository.save(verification);
    }

    // 인증번호 확인
    public boolean checkVerificationNumber(CheckVerificationReq checkVerificationReq) {
        String username = checkVerificationReq.getUsername();
        String verificationNumber = checkVerificationReq.getVerificationNumber();

        Verification verification = verificationRepository.findByUsername(username);
        if (verification == null) {
            return false;
        }
        return verification.getUsername().equals(username)
                && verification.getVerificationNumber().equals(verificationNumber)
                && LocalDateTime.now().isBefore(verification.getExpirationTime());
    }

    public String getVerificationNumber() {
        String verificationNumber = "";

        for (int num=0; num<4; num++) {
            verificationNumber += (int) (Math.random()*10); // 0~9까지의 랜덤 정수 생성
        }

        return verificationNumber;
    }


    // 인증번호 검증
    public void verifyEmail(EmailVerificationReq emailVerificationReq) {
//        Integer userId = emailVerificationReq.getUserId();
        String email = emailVerificationReq.getUsername();


    }

}

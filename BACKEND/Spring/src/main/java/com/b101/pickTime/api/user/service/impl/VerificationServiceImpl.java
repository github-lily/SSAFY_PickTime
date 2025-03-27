package com.b101.pickTime.api.user.service.impl;

import com.b101.pickTime.api.user.request.CheckVerificationReq;
import com.b101.pickTime.api.user.request.EmailVerificationReq;
import com.b101.pickTime.api.user.service.VerificationService;
import com.b101.pickTime.common.email.EmailSender;
import com.b101.pickTime.common.exception.exception.NotFoundUsernameException;
import com.b101.pickTime.db.entity.Verification;
import com.b101.pickTime.db.repository.UserRepository;
import com.b101.pickTime.db.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {
    private final int EXPIRATION_SECONDS = 60 * 3; // 유효시간 3분

    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;

    // 인증메일 전송
    public void sendVerificationEmail(EmailVerificationReq emailVerificationReq) {
        String username = emailVerificationReq.getUsername();
        // 해당 이메일 존재하는지 확인
        if (!userRepository.existsByUsername(username)) {
            throw new NotFoundUsernameException();
        }
        // 랜덤 인증번호 생성
        String verificationNumber = createVerificationNumber();
        // DB에 인증정보 저장
        createVerification(username, verificationNumber);
        // 이메일 전송
        emailSender.sendVerificationMail(username, verificationNumber);
    }

    // 엔티티 생성 및 저장
    private void createVerification(String username, String verificationNumber) {
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
        if (verification.getUsername().equals(username)
                && verification.getVerificationNumber().equals(verificationNumber)
                && LocalDateTime.now().isBefore(verification.getExpirationTime())) {
            // 인증 완료되었으니 DB에서 삭제
            verificationRepository.delete(verification);
            return true;
        }
        return false;
    }

    private String createVerificationNumber() {
        String verificationNumber = "";
        for (int num = 0; num < 4; num++) {
            verificationNumber += (int) (Math.random() * 10); // 0~9까지의 랜덤 정수 생성
        }
        return verificationNumber;
    }
}

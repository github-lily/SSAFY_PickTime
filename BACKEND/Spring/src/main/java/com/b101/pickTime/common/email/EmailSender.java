package com.b101.pickTime.common.email;

import com.b101.pickTime.common.exception.exception.FailedSendEmailException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSender {
    private final JavaMailSender javaMailSender;
    private final String SUBJECT = "[PickTime] 인증 메일입니다";   // 메일 제목
    public void sendVerificationMail(String email, String verificationNumber) {

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

            String htmlContent = getVerificationMessage(verificationNumber);

            messageHelper.setTo(email); // 수신인 설정
            messageHelper.setSubject(SUBJECT); // 제목 설정
            messageHelper.setText(htmlContent, true); // 내용 설정 + html 가능하도록 true 설정

            javaMailSender.send(message);
        } catch (Exception e) {
            throw new FailedSendEmailException("Failed to send email");
        }
    }

    private String getVerificationMessage(String verificationNumber) {
        String verificationMessage = "";
        verificationMessage += "<h1 style='text-align: center;'>[PickTime] 인증메일</h1>";
        verificationMessage += "<h3 style='text-align: center;'>인증코드 : <string style='front-size: 32px; letter-spacing: 8px;'>" + verificationNumber + "</strong></h3>";

        return verificationMessage;
    }
}

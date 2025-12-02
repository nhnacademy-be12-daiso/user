/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2025. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.user.service.message;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MailService {

    private static final SecureRandom random = new SecureRandom();

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String username;    // 발신자 이메일(현재: wlsdud3309@naver.com - 개인 메일)

    public MimeMessage createCode(String email, String code)
            throws MessagingException, UnsupportedEncodingException {   // 휴면 해제 인증번호 메일
        MimeMessage message = javaMailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, email);
        message.setSubject("[Daiso] 휴면 계정 활성화 인증번호 안내");    // 메일 제목

        String mailBody = """
                <div style='margin: 40px; font-family: Arial, sans-serif; line-height: 1.6;'>
                    <h1 style='font-size: 22px; font-weight: bold;'>안녕하세요, Daiso 이용자님.</h1>
                    <p>보안 확인을 위해 휴면 계정 활성화 인증번호를 안내드립니다.</p>
                
                    <div style='margin: 20px 0; padding: 15px; background-color: #f3f4f6; border-radius: 5px; text-align: center;'>
                        <strong style='font-size: 24px; color: #3b82f6; letter-spacing: 2px;'>%s</strong>
                    </div>
                
                    <p>휴면 상태를 해제하고 서비스를 정상적으로 이용하시려면 위 인증번호를 입력해주세요.</p>
                    <p>타인에게 인증번호가 노출되지 않도록 유의해주시기 바랍니다.</p>
                
                    <p style='margin-top: 40px;'>감사합니다.</p>
                    <p><strong>Daiso 드림</strong></p>
                </div>
                """.formatted(code);    // 메일 내용 HTML

        message.setText(mailBody, "utf-8", "html");
        message.setFrom(new InternetAddress(username, "Daiso"));

        return message;
    }

    public MimeMessage createTemporaryPassword(String email, String temporaryPassword)
            throws MessagingException, UnsupportedEncodingException {   // 임시 비밀번호 발급 메일
        MimeMessage message = javaMailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, email);
        message.setSubject("[Daiso] 임시 비밀번호 발급 안내");

        String mailBody = """
                <div style='margin: 40px; font-family: Arial, sans-serif; line-height: 1.6;'>
                    <h1 style='font-size: 22px; font-weight: bold;'>안녕하세요, Daiso 이용자님.</h1>
                    <p>요청하신 임시 비밀번호가 발급되었습니다.</p>
                    <p>로그인 후 반드시 비밀번호를 변경해주시기 바랍니다.</p>
                
                    <div style='margin: 20px 0; padding: 15px; background-color: #f3f4f6; border-radius: 5px; text-align: center;'>
                        <strong style='font-size: 24px; color: #3b82f6; letter-spacing: 2px;'>%s</strong>
                    </div>
                
                    <p>타인에게 비밀번호가 노출되지 않도록 유의해주시기 바랍니다.</p>
                
                    <p style='margin-top: 40px;'>감사합니다.</p>
                    <p><strong>Daiso 드림</strong></p>
                </div>
                """.formatted(temporaryPassword);

        message.setText(mailBody, "utf-8", "html");
        message.setFrom(new InternetAddress(username, "Daiso"));

        return message;
    }

    public String sendCode(String email)
            throws MessagingException, UnsupportedEncodingException {   // 휴면 해제 인증번호 메일 발송
        String code = createCode();

        MimeMessage message = createCode(email, code);

        javaMailSender.send(message);

        return code;
    }

    public void sendTemporaryPassword(String email, String temporaryPassword)
            throws MessagingException, UnsupportedEncodingException {   // 임시 비밀번호 발급 메일 발송
        MimeMessage message = createTemporaryPassword(email, temporaryPassword);

        javaMailSender.send(message);
    }

    public String createCode() {    // 6자리 랜덤 숫자 생성
        return String.valueOf(100000 + random.nextInt(900000));
    }

}

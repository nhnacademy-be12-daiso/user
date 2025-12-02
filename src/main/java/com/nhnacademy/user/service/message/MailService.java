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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String username;    // 발신자 이메일(현재: wlsdud3309@naver.com - 개인 메일)

    public MimeMessage createMessage(String email, String code)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, email);
        message.setSubject("[Daiso] 휴면 계정 활성화 인증번호 안내");    // 메일 제목

        String mailBody = """
                <div style='margin: 40px; font-family: Arial, sans-serif; line-height: 1.6;'>
                    <h1 style='font-size: 22px; font-weight: bold;'>안녕하세요, Daiso 이용자님.</h1>
                    <p>보안 확인을 위해 휴면 계정 활성화 인증번호를 안내드립니다.</p>
                
                    <div style='margin: 20px 0; font-size: 18px;'>
                        <strong style='font-size: 22px;'>%s</strong>
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

    public String sendMessage(String email) throws MessagingException, UnsupportedEncodingException {   // 메일 발송
        String code = createCode();

        MimeMessage message = createMessage(email, code);

        javaMailSender.send(message);

        return code;
    }

    public String createCode() {    // 6자리 랜덤 숫자 생성
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

}

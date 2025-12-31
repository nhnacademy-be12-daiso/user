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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.exception.message.MailSendException;
import com.nhnacademy.user.properties.MailProperties;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MailProperties mailProperties;

    private MailService mailService;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        given(mailProperties.getUsername()).willReturn("test@nhnacademy.com");
        mailService = new MailService(javaMailSender, mailProperties);
    }

    @Test
    @DisplayName("휴면 해제 인증코드 메일 발송 - 성공")
    void test1() {
        String email = "user@example.com";

        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);

        String result = mailService.sendCode(email);

        assertThat(result).hasSize(6);
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("임시 비밀번호 메일 발송 - 성공")
    void test2() {
        String email = "user@example.com";
        String tempPassword = "StrongPassword123!";

        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);

        mailService.sendTemporaryPassword(email, tempPassword);

        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("메일 전송 실패 - JavaMailSender 오류")
    void test3() {
        String email = "fail@test.com";
        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);

        org.mockito.Mockito.doThrow(new org.springframework.mail.MailSendException("SMTP Error"))
                .when(javaMailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> mailService.sendCode(email))
                .isInstanceOf(MailSendException.class)
                .hasMessageContaining("예상치 못한 오류");
    }

    @Test
    @DisplayName("메일 생성 실패 - MessagingException 발생")
    void test4() throws Exception {
        String email = "error@test.com";
        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);

        org.mockito.Mockito.doThrow(new jakarta.mail.MessagingException("Message Error"))
                .when(mimeMessage).setFrom(any(jakarta.mail.internet.InternetAddress.class));

        assertThatThrownBy(() -> mailService.sendCode(email))
                .isInstanceOf(MailSendException.class)
                .hasMessageContaining("예상치 못한 오류");
    }

}

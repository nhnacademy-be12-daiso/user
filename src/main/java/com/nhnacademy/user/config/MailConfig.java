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

package com.nhnacademy.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;


@Slf4j
@Configuration
public class MailConfig {

    // SMTP 서버
    @Value("${spring.mail.host}")
    private String host;

    // 포트
    @Value("${spring.mail.port}")
    private Integer port;

    // 계정
    @Value("${spring.mail.username}")
    private String username;

    // 비밀번호
    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public JavaMailSender javaMailService() {   // JavaMailSender 빈 등록
        log.info("[MailConfig] JavaMailSender 빈 생성 시작");
        log.info("[MailConfig] SMTP 호스트: {}", host);
        log.info("[MailConfig] SMTP 포트: {}", port);
        log.info("[MailConfig] 사용자명(이메일): {}", username);
        log.info("[MailConfig] 비밀번호 설정 여부: {}", password != null && !password.isEmpty());

        final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setHost(host);
        javaMailSender.setPort(port);
        javaMailSender.setUsername(username);
        javaMailSender.setPassword(password);
        javaMailSender.setJavaMailProperties(getMailProperties());

        log.info("[MailConfig] JavaMailSender 빈 생성 완료");

        return javaMailSender;
    }

    private Properties getMailProperties() {
        Properties properties = new Properties();

        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.ssl.enable", "true");
        properties.setProperty("mail.smtp.ssl.trust", host);
        properties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.setProperty("mail.smtp.socketFactory.port", String.valueOf(port));
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        properties.setProperty("mail.smtp.starttls.enable", "false");
        properties.setProperty("mail.smtp.starttls.required", "false");
        properties.setProperty("mail.debug", "true");
        properties.setProperty("mail.smtp.timeout", "5000");
        properties.setProperty("mail.smtp.connectiontimeout", "5000");
        
        log.info("[MailConfig] SMTP 속성 설정 완료 - SSL: true, SocketFactory: SSLSocketFactory, TLS: 1.2");

        return properties;
    }

}

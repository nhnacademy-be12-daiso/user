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
        log.info("[MailConfig] 비밀번호 길이: {}", password != null ? password.length() : 0);

        // 비밀번호에 공백이나 특수문자가 있는지 확인
        if (password != null) {
            boolean hasSpace = password.contains(" ");
            boolean startsWithQuote = password.startsWith("'") || password.startsWith("\"");
            boolean endsWithQuote = password.endsWith("'") || password.endsWith("\"");
            log.info("[MailConfig] 비밀번호 검증 - 공백포함: {}, 따옴표시작: {}, 따옴표끝: {}",
                    hasSpace, startsWithQuote, endsWithQuote);

            if (startsWithQuote || endsWithQuote) {
                log.warn("[MailConfig] 경고: 비밀번호에 따옴표가 포함되어 있습니다. .env 파일에서 따옴표를 제거하세요!");
            }
        }

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

        // 기본 SMTP 설정
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "true");

        // SSL 설정
        properties.setProperty("mail.smtp.ssl.enable", "true");
        properties.setProperty("mail.smtp.ssl.trust", host);
        properties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

        // SocketFactory 설정
        properties.setProperty("mail.smtp.socketFactory.port", String.valueOf(port));
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");

        // STARTTLS 비활성화
        properties.setProperty("mail.smtp.starttls.enable", "false");
        properties.setProperty("mail.smtp.starttls.required", "false");

        // 인증 메커니즘 설정 (네이버는 PLAIN 또는 LOGIN 지원)
        properties.setProperty("mail.smtp.auth.mechanisms", "PLAIN LOGIN");

        // 인코딩 설정
        properties.setProperty("mail.mime.charset", "UTF-8");
        properties.setProperty("mail.smtp.ehlo", "true");

        // 타임아웃 설정
        properties.setProperty("mail.smtp.timeout", "10000");
        properties.setProperty("mail.smtp.connectiontimeout", "10000");
        properties.setProperty("mail.smtp.writetimeout", "10000");

        // 디버그 모드
        properties.setProperty("mail.debug", "true");

        log.info("[MailConfig] SMTP 속성 설정 완료 - SSL: true, Auth: PLAIN LOGIN, Port: {}", port);

        return properties;
    }

}

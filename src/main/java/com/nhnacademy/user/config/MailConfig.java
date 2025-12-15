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

import com.nhnacademy.user.properties.MailProperties;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


@Slf4j
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

    // SMTP 서버
    private final String host;

    // 포트
    private final Integer port;

    // 계정
    private final String username;

    // 비밀번호
    private final String password;

    public MailConfig(MailProperties mailProperties) {
        this.host = mailProperties.getHost();
        this.port = mailProperties.getPort();
        this.username = mailProperties.getUsername();
        this.password = mailProperties.getPassword();
    }

    @Bean
    public JavaMailSender javaMailService() {   // JavaMailSender 빈 등록
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setHost(host);
        javaMailSender.setPort(port);
        javaMailSender.setUsername(username);
        javaMailSender.setPassword(password);

        javaMailSender.setJavaMailProperties(getMailProperties());

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

        return properties;
    }

}

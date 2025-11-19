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

package com.nhnacademy.user.util;

import com.nhnacademy.user.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    // JWT 서명에 사용할 비밀키
    private final Key key;

    // 토큰 만료 시간
    private final long expirationTime;

    // 토큰 앞에 붙는 접두사 (현재: Daiso)
    private final String tokenPrefix;

    public JwtUtil(JwtProperties jwtProperties) {
        // 비밀키를 HMAC SHA 알고리즘용 Key 객체로 변환
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        // 초 단위로 설정되어 있는 expirationTime을 ms 단위로 변환
        this.expirationTime = jwtProperties.getExpirationTime() * 1000L;
        this.tokenPrefix = jwtProperties.getTokenPrefix();
    }

    // JWT Access Token 생성
    public String createAccessToken(String loginId, String role) {
        String token = Jwts.builder()
                .setSubject(loginId)    // 토큰 소유자를 나타내는 고유 식별자
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)    // 서명 알고리즘
                .compact();

        // Daiso {token}
        return tokenPrefix + " " + token;
    }

}

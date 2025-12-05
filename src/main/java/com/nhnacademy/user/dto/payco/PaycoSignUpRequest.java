package com.nhnacademy.user.dto.payco;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaycoSignUpRequest {
    private String paycoIdNo;   // 필수
    private String email;       // 선택
    private String mobile;      // 선택
    private String name;        // 선택
}
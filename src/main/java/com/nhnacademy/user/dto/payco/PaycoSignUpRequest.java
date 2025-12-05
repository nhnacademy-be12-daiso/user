package com.nhnacademy.user.dto.payco;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaycoSignUpRequest {
    @NotBlank(message = "Payco 회원 번호는 필수입니다")
    private String paycoIdNo;
}
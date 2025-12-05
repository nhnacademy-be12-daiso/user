package com.nhnacademy.user.dto.payco;

import com.nhnacademy.user.common.ValidationUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaycoSignUpRequest {
    @NotBlank(message = "Payco 회원 번호는 필수입니다")
    private String paycoIdNo;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "휴대폰 번호는 필수입니다")
    @Pattern(regexp = ValidationUtils.PHONE_PATTERN, message = "올바르지 않은 형식의 연락처입니다")
    private String mobile;

    @Size(max = 30, message = "이름은 30자 이내여야 합니다")
    private String name;
}
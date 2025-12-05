package com.nhnacademy.user.dto.payco;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaycoLoginResponse {
    @NotNull
    private Long userCreatedId;

    @NotBlank
    private String loginId;

    @NotBlank
    private String role;

    private boolean isNewUser;
}
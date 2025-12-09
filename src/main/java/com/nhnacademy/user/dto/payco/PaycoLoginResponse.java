package com.nhnacademy.user.dto.payco;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaycoLoginResponse {
    private Long userCreatedId;
    private String loginId;
    private String role;
    private boolean isNewUser;
    private boolean needsProfileCompletion;  // 프로필 완성 필요 여부
}
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

package com.nhnacademy.user.dto.request;

import com.nhnacademy.user.common.ValidationUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

// 아이디: 영문 소문자 + 숫자, 3~16자
// 비밀번호: 최소 1개의 영문 + 최소 1개의 숫자 + 최소 1개의 특수문자, 8~20자
// 연락처: 010-xxxx-xxxx 형식
// 이메일: xxx@yyy.zzz 형식
public record SignupRequest(@NotBlank @Pattern(regexp = ValidationUtils.LOGIN_ID_PATTERN) String loginId,
                            @NotBlank @Pattern(regexp = ValidationUtils.PASSWORD_PATTERN) String password,
                            @NotBlank String userName,
                            @NotBlank @Pattern(regexp = ValidationUtils.PHONE_PATTERN) String phoneNumber,
                            @NotBlank @Email String email,
                            @NotNull LocalDate birth) {
    // 클라이언트로부터 회원가입 요청 데이터를 받기 위한 요청 DTO
}

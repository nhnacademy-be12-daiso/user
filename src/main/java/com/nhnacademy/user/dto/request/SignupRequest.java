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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

// 아이디: 영문 소문자 + 숫자, 3~16자
// 비밀번호: 최소 1개의 영문 + 최소 1개의 숫자 + 최소 1개의 특수문자, 8~20자
// 연락처: 010-xxxx-xxxx 형식
// 이메일: xxx@yyy.zzz 형식
public record SignupRequest(
        @NotBlank(message = "아이디는 필수 입력 값입니다.") @Pattern(regexp = ValidationUtils.LOGIN_ID_PATTERN, message = "올바르지 않은 형식의 아이디입니다.") String loginId,
        @NotBlank(message = "비밀번호는 필수 입력 값입니다.") @Pattern(regexp = ValidationUtils.PASSWORD_PATTERN, message = "올바르지 않은 형식의 비밀번호입니다.") String password,
        @NotBlank(message = "이름은 필수 입력 값입니다.") String userName,
        @NotBlank(message = "연락처는 필수 입력 값입니다.") @Pattern(regexp = ValidationUtils.PHONE_PATTERN, message = "올바르지 않은 형식의 연락처입니다.") String phoneNumber,
        @NotBlank(message = "이메일은 필수 입력 값입니다.") @Pattern(regexp = ValidationUtils.EMAIL_PATTERN, message = "올바르지 않은 이메일 형식입니다.") String email,
        @NotNull(message = "생년월일은 필수 입력 값입니다.") @Past(message = "생년월일은 현재 날짜보다 과거여야 합니다.") LocalDate birth) {
    // 클라이언트로부터 회원가입 요청 데이터를 받기 위한 요청 DTO
}

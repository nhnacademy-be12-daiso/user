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
import jakarta.validation.constraints.Pattern;

public record FindPasswordRequest(
        @NotBlank(message = "아이디는 필수 입력 값입니다.") @Pattern(regexp = ValidationUtils.LOGIN_ID_PATTERN, message = "올바르지 않은 아이디 형식입니다.") String loginId,
        @NotBlank(message = "이름은 필수 입력 값입니다.") String userName,
        @NotBlank(message = "이메일은 필수 입력 값입니다.") @Pattern(regexp = ValidationUtils.EMAIL_PATTERN, message = "올바르지 않은 이메일 형식입니다.") String email) {
    // 비밀번호 찾기 요청 DTO
}

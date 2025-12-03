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
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record UserModifyRequest(@NotBlank(message = "이름은 필수 입력 값입니다.") String userName,
                                @NotBlank(message = "연락처는 필수 입력 값입니다.") @Pattern(regexp = ValidationUtils.PHONE_PATTERN, message = "올바르지 않은 형식의 연락처입니다.") String phoneNumber,
                                @NotBlank(message = "이메일은 필수 입력 값입니다.") @Email(message = "올바르지 않은 형식의 이메일입니다.") String email,
                                @NotNull(message = "생년월일은 필수 입력 값입니다.") @Past(message = "생년월일은 현재 날짜보다 과거여야 합니다.") LocalDate birth) {
    // 클라이언트로부터 수정할 회원 정보 데이터를 받기 위한 요청 DTO
}

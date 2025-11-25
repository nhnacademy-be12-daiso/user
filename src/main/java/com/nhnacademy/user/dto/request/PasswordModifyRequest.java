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

// 새로운 비밀번호: 최소 1개의 영문 + 최소 1개의 숫자 + 최소 1개의 특수문자, 8~20자
public record PasswordModifyRequest(@NotBlank String currentPassword,
                                    @NotBlank @Pattern(regexp = ValidationUtils.PASSWORD_PATTERN) String newPassword) {
    // 클라이언트로부터 변경할 비밀번호 데이터를 받기 위한 요청 DTO
}

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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// 아이디: 영문 소문자 + 숫자, 3~16자
// 비밀번호: 로그인용이기 때문에 빈 칸만 아니면 됨
public record LoginRequest(@NotBlank @Pattern(regexp = "^[a-z0-9]{3,16}$") String loginId,
                           @NotBlank String password) {
    // 클라이언트로부터 로그인 요청 데이터를 받기 위한 요청 DTO
}

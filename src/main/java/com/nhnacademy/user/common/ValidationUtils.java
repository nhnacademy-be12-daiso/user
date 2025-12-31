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

package com.nhnacademy.user.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)    // 인스턴스화 방지
public final class ValidationUtils {  // 정규식 패턴 상수 클래스
    // 보안 정책이 바뀌었을 때 해당 클래스의 상수만 수정하면 됨

    // 소문자 1개 이상, 3~16자
    // 소문자, 숫자, 언더바 허용
    public static final String LOGIN_ID_PATTERN = "^(?=.*[a-z])[a-z0-9_]{3,16}$";

    // 영문 1개 이상, 숫자 1개 이상, 특수문자 1개 이상, 8~20자
    @SuppressWarnings("java:S2068")
    public static final String PASSWORD_VALIDATION_PATTERN =
            "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"|,.<>/?]).{8,20}$";

    // 010-XXXX-XXXX
    public static final String PHONE_PATTERN = "^010-\\d{3,4}-\\d{4}$";

    // example@example.com (.co.kr처럼 점이 여러 개여도 괜찮음)
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

}

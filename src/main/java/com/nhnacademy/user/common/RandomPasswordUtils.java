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

import java.security.SecureRandom;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)    // 인스턴스화 방지
public final class RandomPasswordUtils {  // 랜덤 비밀번호 생성 클래스

    private static final SecureRandom random = new SecureRandom();

    private static final int LENGTH = 12;

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGIT = "0123456789";
    private static final String SPECIAL = "!@#$%^&*";

    private static final String ALL = LOWER + UPPER + DIGIT + SPECIAL;

    public static String createTemporaryPassword() {
        char[] password = new char[LENGTH];

        // 최소 1개의 소문자, 대문자, 숫자, 특수문자가 포함
        password[0] = LOWER.charAt(random.nextInt(LOWER.length()));
        password[1] = UPPER.charAt(random.nextInt(UPPER.length()));
        password[2] = DIGIT.charAt(random.nextInt(DIGIT.length()));
        password[3] = SPECIAL.charAt(random.nextInt(SPECIAL.length()));

        // 나머지는 랜덤
        for (int i = 4; i < LENGTH; i++) {
            password[i] = ALL.charAt(random.nextInt(ALL.length()));
        }

        // 문자열 섞기 (Fisher-Yates shuffle 알고리즘)
        for (int i = password.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);

            char temp = password[i];
            password[i] = password[j];
            password[j] = temp;
        }

        return new String(password);
    }

}

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomPasswordUtils {  // 랜덤 비밀번호 생성 클래스

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String DIGIT = "0123456789";

    private static final String SPECIAL = "!@#$%^&*";

    private static final String ALL = LOWER + UPPER + DIGIT + SPECIAL;

    private static final int LENGTH = 12;

    private static final SecureRandom random = new SecureRandom();

    public static String createTemporaryPassword() {
        StringBuilder sb = new StringBuilder(LENGTH);

        // 최소 1개의 소문자, 대문자, 숫자, 특수문자가 포함
        sb.append(LOWER.charAt(random.nextInt(LOWER.length())));
        sb.append(UPPER.charAt(random.nextInt(UPPER.length())));
        sb.append(DIGIT.charAt(random.nextInt(DIGIT.length())));
        sb.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // 나머지는 랜덤
        for (int i = 4; i < LENGTH; i++) {
            sb.append(ALL.charAt(random.nextInt(ALL.length())));
        }

        List<Character> characterList = new ArrayList<>();

        for (char c : sb.toString().toCharArray()) {
            characterList.add(c);
        }

        Collections.shuffle(characterList, random);

        StringBuilder result = new StringBuilder();

        for (char c : characterList) {
            result.append(c);
        }

        return result.toString();
    }

}

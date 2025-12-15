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
public final class MaskingUtils { // 아이디 마스킹

    public static String maskLoginId(String loginId) {
        if (loginId == null || loginId.length() < 2) {
            return loginId;
        }

        int length = loginId.length();

        int visibleLength = (length > 3) ? length - 3 : 1;

        return loginId.substring(0, visibleLength) + "*".repeat(length - visibleLength);
    }

}

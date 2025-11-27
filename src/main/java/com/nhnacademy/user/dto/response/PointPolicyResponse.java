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

package com.nhnacademy.user.dto.response;

import com.nhnacademy.user.entity.point.Method;
import java.math.BigDecimal;

public record PointPolicyResponse(Long pointPolicyId,
                                  String policyName,
                                  String policyType,
                                  Method method,
                                  BigDecimal earnPoint) {
    // 관리자 전용, 포인트 정책 관련 데이터를 받을 DTO
}

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

import com.nhnacademy.user.entity.point.Type;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PointRequest(@NotNull Long userCreatedId,
                           @NotNull(message = "금액은 필수 입력 값입니다.") @Min(value = 1, message = "금액은 1 이상이어야 합니다.") BigDecimal amount,
                           @NotNull Type type,
                           String description) {
    // 포인트 변동 요청 시 사용하는 DTO
}

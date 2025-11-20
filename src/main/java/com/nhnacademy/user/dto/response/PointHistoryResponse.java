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

import com.nhnacademy.user.entity.point.PointHistory;
import com.nhnacademy.user.entity.point.Type;
import java.time.LocalDateTime;

public record PointHistoryResponse(Long pointHistoryId,
                                   long amount,
                                   Type type,
                                   String description,
                                   LocalDateTime createdAt) {
    // 프론트엔드 조회용 응답 DTO

    // PointHistory 엔티티 > DTO 변환 메소드
    public static PointHistoryResponse fromEntity(PointHistory pointHistory) {
        return new PointHistoryResponse(pointHistory.getPointHistoryId(), pointHistory.getAmount(),
                pointHistory.getType(), pointHistory.getDescription(), pointHistory.getCreatedAt());
    }

}

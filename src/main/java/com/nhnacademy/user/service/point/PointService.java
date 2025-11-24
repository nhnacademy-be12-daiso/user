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

package com.nhnacademy.user.service.point;

import com.nhnacademy.user.dto.request.PointRequest;
import com.nhnacademy.user.dto.response.PointHistoryResponse;
import com.nhnacademy.user.dto.response.PointResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointService {

    // 현재 내 포인트 잔액 조회
    PointResponse getCurrentPoint(String loginId);

    // 정책 기반 포인트 적립
    void earnPointByPolicy(String loginId, String policyType);

    // 포인트 변동 수동 처리
    void processPoint(PointRequest request);

    // 내 포인트 내역 조회
    Page<PointHistoryResponse> getMyPointHistory(String loginId, Pageable pageable);

}

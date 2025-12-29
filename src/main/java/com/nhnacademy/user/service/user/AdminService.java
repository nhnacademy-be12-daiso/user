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

package com.nhnacademy.user.service.user;

import com.nhnacademy.user.dto.request.AccountStatusRequest;
import com.nhnacademy.user.dto.request.UserGradeRequest;
import com.nhnacademy.user.dto.response.UserDetailResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.dto.search.UserSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    // 전체 회원 목록 조회(페이징)
    Page<UserResponse> getAllUsers(Pageable pageable, UserSearchCriteria criteria);

    // 특정 회원 상세 조회
    UserDetailResponse getUserDetail(Long userCreatedId);

    // 회원 상태 변경
    void modifyAccountStatus(Long adminId, Long userId, AccountStatusRequest request);

    // 회원 등급 변경
    void modifyUserGrade(Long adminId, Long userId, UserGradeRequest request);

}

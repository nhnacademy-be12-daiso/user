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

import com.nhnacademy.user.dto.request.LoginRequest;
import com.nhnacademy.user.dto.request.PasswordModifyRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.request.UserModifyRequest;
import com.nhnacademy.user.dto.response.UserResponse;

public interface UserService {

    // 회원가입
    void signUp(SignupRequest request);

    // 로그인
    String login(LoginRequest request);

    // 회원 정보 조회
    UserResponse getUserInfo(String loginId);

    // 회원 정보 수정
    void modifyUserInfo(String loginId, UserModifyRequest request);

    // 비밀번호 수정
    void modifyUserPassword(String loginId, PasswordModifyRequest request);

    // 회원 탈퇴(회원 상태를 WITHDRAWN으로 바꿈)
    void withdrawUser(String loginId);

    // 휴면 계정 전환 배치 작업
    void dormantAccounts();

    // 휴면 계정 복구
    void activeUser(String loginId);

}

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

package com.nhnacademy.user.entity.account;

import com.nhnacademy.user.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {      // 회원 인증 (로그인) 정보

    @Id
    @Column(name = "login_id", length = 16)
    private String loginId;         // 로그인 아이디: 사용자 입력 (PK)

    @Column                         // DB에는 인코딩 된 값이 들어감
    private String password;        // 로그인 비밀번호: 사용자 입력

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;  // 계정 권한, default = 'USER'

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_created_id", unique = true)
    private User user;              // Users 테이블 외래키 (FK), 일대일 관계

    public Account(String loginId, String password, Role role, User user) {
        this.loginId = loginId;
        this.password = password;
        this.role = role;
        this.user = user;
    }

    public void modifyPassword(String password) {   // 비밀번호를 변경하는 메소드
        this.password = password;
    }

}

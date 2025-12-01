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

package com.nhnacademy.user.entity.user;

import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.address.Address;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {     // 회원 기본 정보

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_created_id")
    private Long userCreatedId;             // 회원 고유 ID (PK, AI)

    @Column(name = "user_name", nullable = false, length = 30)
    private String userName;                // 회원 이름

    @Column(name = "phone_number", unique = true, length = 30)
    private String phoneNumber;             // 연락처 (UK)

    @Column(unique = true, length = 80)
    private String email;                    // 이메일 (UK)

    private LocalDate birth;                 // 생년월일, YYYY-MM-DD 패턴

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Account account;                // 연관 계정, 일대일 관계, User 삭제 시 Account 함께 삭제

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Address> addresses = new ArrayList<>();    // 배송지 리스트, 일대다 관계, User 삭제 시 Addresses 함께 삭제

    public User(String userName, String phoneNumber, String email, LocalDate birth) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.birth = birth;
    }

    public void modifyInfo(String userName, String phoneNumber, String email, LocalDate birth) {    // 회원 정보를 수정하는 메소드
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.birth = birth;
    }

}

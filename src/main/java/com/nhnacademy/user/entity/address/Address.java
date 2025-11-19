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

package com.nhnacademy.user.entity.address;

import com.nhnacademy.user.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "Addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {      // 회원 배송지 주소

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;         // 주소 고유 ID (PK, AI)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_created_id", nullable = false)
    private User user;              // Users 테이블 외래키 (FK), 다대일 관계

    @Length(max = 50)
    @Column(name = "address_name", nullable = false)
    private String addressName;     // 주소 별칭

    @Length(max = 255)
    @Column(name = "address_detail", nullable = false)
    private String addressDetail;   // 전체 주소

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;      // 기본 배송지 여부

    public Address(User user, String addressName, String addressDetail, boolean isDefault) {
        this.user = user;
        this.addressName = addressName;
        this.addressDetail = addressDetail;
        this.isDefault = isDefault;
    }

    public void modifyDetails(String addressName, String addressDetail, boolean isDefault) {
        this.addressName = addressName;
        this.addressDetail = addressDetail;
        this.isDefault = isDefault;
    }

}

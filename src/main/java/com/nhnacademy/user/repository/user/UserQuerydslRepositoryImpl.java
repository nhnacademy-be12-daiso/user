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

package com.nhnacademy.user.repository.user;

import static com.nhnacademy.user.entity.account.QAccount.account;
import static com.nhnacademy.user.entity.point.QPointHistory.pointHistory;
import static com.nhnacademy.user.entity.user.QGrade.grade;
import static com.nhnacademy.user.entity.user.QStatus.status;
import static com.nhnacademy.user.entity.user.QUser.user;
import static com.nhnacademy.user.entity.user.QUserGradeHistory.userGradeHistory;
import static com.nhnacademy.user.entity.user.QUserStatusHistory.userStatusHistory;

import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.entity.point.Type;
import com.nhnacademy.user.entity.user.QUserGradeHistory;
import com.nhnacademy.user.entity.user.QUserStatusHistory;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserQuerydslRepositoryImpl implements UserQuerydslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<UserResponse> findAllUser(Pageable pageable) {
        QUserGradeHistory subGradeHistory = new QUserGradeHistory("subGradeHistory");
        QUserStatusHistory subStatusHistory = new QUserStatusHistory("subStatusHistory");

        // 데이터 조회 쿼리
        List<UserResponse> content = jpaQueryFactory
                .select(Projections.constructor(UserResponse.class,
                        account.loginId,
                        user.userName,
                        user.phoneNumber,
                        user.email,
                        user.birth,
                        // [서브쿼리 1] 가장 최근 날짜를 찾아서 그 날짜의 등급 데이터를 가져옴
                        JPAExpressions.select(grade.gradeName)
                                .from(userGradeHistory)
                                .join(userGradeHistory.grade, grade)
                                .where(userGradeHistory.user.eq(user)
                                        .and(userGradeHistory.changedAt.eq(
                                                JPAExpressions.select(subGradeHistory.changedAt.max())
                                                        .from(subGradeHistory)
                                                        .where(subGradeHistory.user.eq(user))
                                        ))),
                        // [서브쿼리 2] 포인트 잔액 계산 (SUM)
                        JPAExpressions.select(
                                        // new CaseBuilder()를 사용해야 합니다.
                                        new CaseBuilder()
                                                .when(pointHistory.type.eq(Type.USE))
                                                .then(pointHistory.amount.negate()) // 사용이면 -
                                                .otherwise(pointHistory.amount)     // 아니면 +
                                                .sum()
                                                .coalesce(BigDecimal.ZERO) // null이면 0
                                )
                                .from(pointHistory)
                                .where(pointHistory.user.eq(user)),
                        // [서브쿼리 3] 가장 최근 날짜를 찾아서 그 날짜의 상태 데이터를 가져옴
                        JPAExpressions.select(status.statusName)
                                .from(userStatusHistory)
                                .join(userStatusHistory.status, status)
                                .where(userStatusHistory.user.eq(user)
                                        .and(userStatusHistory.changedAt.eq(
                                                JPAExpressions.select(subStatusHistory.changedAt.max())
                                                        .from(subStatusHistory)
                                                        .where(subStatusHistory.user.eq(user))
                                        ))),
                        user.joinedAt
                ))
                .from(user)
                .join(user.account, account)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리
        Long count = jpaQueryFactory
                .select(user.count())
                .from(user)
                .fetchOne();

        return new PageImpl<>(content, pageable, count != null ? count : 0);
    }

}

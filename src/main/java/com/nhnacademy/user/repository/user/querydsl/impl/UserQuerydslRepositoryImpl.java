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

package com.nhnacademy.user.repository.user.querydsl.impl;

import static com.nhnacademy.user.entity.account.QAccount.account;
import static com.nhnacademy.user.entity.account.QAccountStatusHistory.accountStatusHistory;
import static com.nhnacademy.user.entity.account.QStatus.status;
import static com.nhnacademy.user.entity.point.QPointHistory.pointHistory;
import static com.nhnacademy.user.entity.user.QGrade.grade;
import static com.nhnacademy.user.entity.user.QUser.user;
import static com.nhnacademy.user.entity.user.QUserGradeHistory.userGradeHistory;

import com.nhnacademy.user.dto.request.UserSearchCriteria;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.entity.account.QAccountStatusHistory;
import com.nhnacademy.user.entity.point.Type;
import com.nhnacademy.user.entity.user.QUserGradeHistory;
import com.nhnacademy.user.repository.user.querydsl.UserQuerydslRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Repository
public class UserQuerydslRepositoryImpl implements UserQuerydslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<UserResponse> findAllUser(Pageable pageable, UserSearchCriteria criteria) {
        // querydsl에서 동일한 엔티티를 from 절에 두 번 이상 사용할 때
        // 서브쿼리 전용 별칭 선언
        QUserGradeHistory subGradeHistory = new QUserGradeHistory("subGradeHistory");
        QAccountStatusHistory subStatusHistory = new QAccountStatusHistory("subStatusHistory");
        // 같은 테이블을 메인 쿼리와 서브 쿼리에서 동시에 사용하기 위한 규칙

        // 데이터 조회 쿼리
        List<UserResponse> content = jpaQueryFactory
                .select(Projections.constructor(UserResponse.class,
                        user.userCreatedId,
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
                                .from(accountStatusHistory)
                                .join(accountStatusHistory.status, status)
                                .where(accountStatusHistory.account.eq(account)
                                        .and(accountStatusHistory.changedAt.eq(
                                                JPAExpressions.select(subStatusHistory.changedAt.max())
                                                        .from(subStatusHistory)
                                                        .where(subStatusHistory.account.eq(account))
                                        ))),
                        account.joinedAt
                ))
                .from(user)
                .join(user.account, account)
                .where(containsKeyword(criteria.keyword()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .fetch();

        // 카운트 쿼리
        Long count = jpaQueryFactory
                .select(user.count())
                .from(user)
                .join(user.account, account)
                .where(containsKeyword(criteria.keyword()))
                .fetchOne();

        return new PageImpl<>(content, pageable, count != null ? count : 0);
    }

    private BooleanBuilder containsKeyword(String keyword) {
        BooleanBuilder builder = new BooleanBuilder();

        if (!StringUtils.hasText(keyword)) {
            return builder;
        }

        builder.or(user.userName.contains(keyword))
                .or(user.email.contains(keyword))
                .or(account.loginId.contains(keyword));

        return builder;
    }

    private OrderSpecifier[] getOrderSpecifier(Sort sort) {
        if (sort.isEmpty()) {
            return new OrderSpecifier[] {user.userCreatedId.desc()}; // 기본 정렬: 최신순
        }

        return sort.stream().map(order -> {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String prop = order.getProperty();

            // 정렬 기준이 되는 QClass 경로 설정
            PathBuilder<?> pathBuilder = new PathBuilder<>(user.getType(), user.getMetadata());

            // 예외적으로 다른 테이블(Account) 정렬이 필요한 경우 처리
            if ("joinedAt".equals(prop)) {
                pathBuilder = new PathBuilder<>(account.getType(), account.getMetadata());
            }

            return new OrderSpecifier(direction, pathBuilder.get(prop));
        }).toArray(OrderSpecifier[]::new);
    }

}

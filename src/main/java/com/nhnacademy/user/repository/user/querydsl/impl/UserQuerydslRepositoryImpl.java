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
import static com.nhnacademy.user.entity.user.QUser.user;

import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.dto.search.UserSearchCriteria;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.repository.user.querydsl.UserQuerydslRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
        // 데이터 조회 쿼리
        List<UserResponse> content = jpaQueryFactory
                .select(Projections.constructor(UserResponse.class,
                        user.userCreatedId,
                        account.loginId,
                        user.userName,
                        user.phoneNumber,
                        user.email,
                        user.birth,
                        user.grade.gradeName,
                        user.currentPoint,
                        account.status.statusName,
                        account.joinedAt
                ))
                .from(user)
                .join(user.account, account)
                .join(user.grade)
                .join(account.status)
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

    private OrderSpecifier<?>[] getOrderSpecifier(Sort sort) {
        if (sort.isEmpty()) {
            return new OrderSpecifier<?>[] {
                    user.userCreatedId.desc()
            };
        }

        return sort.stream().map(order -> {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String prop = order.getProperty();

            // 1. Account 테이블 정렬 (가입일)
            if ("joinedAt".equals(prop)) {
                return new OrderSpecifier<>(direction, account.joinedAt);
            }

            // 2. User 테이블 정렬
            PathBuilder<User> pathBuilder = new PathBuilder<>(user.getType(), user.getMetadata());

            try {
                return new OrderSpecifier<>(direction, pathBuilder.get(prop, String.class));

            } catch (IllegalArgumentException e) {
                // 이상한 필드명(해킹 시도 등)이 들어오면 기본 정렬로 무시하거나 예외 처리
                return new OrderSpecifier<>(Order.DESC, user.userCreatedId);
            }
        }).toArray(OrderSpecifier<?>[]::new);
    }

}

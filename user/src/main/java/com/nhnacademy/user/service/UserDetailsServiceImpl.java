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

package com.nhnacademy.user.service;

import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.user.Status;
import com.nhnacademy.user.repository.account.AccountRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    // AuthenticationManager에서 이 서비스를 호출해 인증 처리

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) {
        Account account = accountRepository.findByIdWithUser(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with loginId: " + loginId));

        // 계정 상태 검증
        if (account.getUser().getStatus() != Status.ACTIVE) {
            throw new UsernameNotFoundException("User account is not active: " + account.getUser().getStatus());
        }

        // spring security User 객체 생성
        // 로그인 ID와 암호화된 비밀번호 사용, 권한 정보(ROLE_ADMIN || ROLE_USER) 부여
        return new User(account.getLoginId(), account.getPassword(),
                // 권한 리스트를 간단하게 생성(Collections.singleton)
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())));
    }

}

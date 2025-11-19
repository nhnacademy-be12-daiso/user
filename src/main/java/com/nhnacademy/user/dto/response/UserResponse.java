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

package com.nhnacademy.user.dto.response;

import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(String userName,
                           String phoneNumber,
                           String email,
                           LocalDate birth,
                           Grade grade,
                           long point,
                           LocalDateTime joinedAt) {
    // 클라이언트에게 반환할 회원 정보를 담는 응답 DTO

    // User 엔티티 > DTO 변환 메소드
    public static UserResponse fromEntity(User user) {
        return new UserResponse(user.getUserName(), user.getPhoneNumber(), user.getEmail(),
                user.getBirth(), user.getGrade(), user.getPoint(), user.getJoinedAt());
    }

}

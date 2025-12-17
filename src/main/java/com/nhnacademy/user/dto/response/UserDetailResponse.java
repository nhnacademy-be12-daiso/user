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

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserDetailResponse(Long userCreatedId,
                                 String loginId,
                                 String userName,
                                 String phoneNumber,
                                 String email,
                                 LocalDate birth,
                                 String statusName,
                                 String gradeName,
                                 String role,
                                 Long currentPoint,
                                 LocalDateTime joinedAt,
                                 LocalDateTime lastLoginAt) {
    // 관리자용, 회원 상세 조회 DTO
}

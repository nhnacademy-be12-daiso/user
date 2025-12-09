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

import java.math.BigDecimal;
import java.util.List;

public record InternalUserResponse(Long userCreatedId,
                                   String userName,
                                   String phoneNumber,
                                   String email,
                                   String gradeName,
                                   BigDecimal pointRate,
                                   BigDecimal point,
                                   List<InternalAddressResponse> addresses) {
    // 주문 서비스가 결제 화면을 그릴 때 원하는 정보만 담기 위해 필요한 DTO
}

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

package com.nhnacademy.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(@NotBlank(message = "별칭은 필수 입력 값입니다.") String addressName,
                             @NotBlank(message = "우편 번호는 필수 입력 값입니다.") String zipCode,
                             @NotBlank(message = "도로명 주소는 필수 입력 값입니다.") String roadAddress,
                             @NotBlank(message = "상세 주소는 필수 입력 값입니다.") String addressDetail,
                             boolean isDefault) {
    // 클라이언트로부터 새 배송지 추가 데이터를 받기 위한 요청 DTO
}

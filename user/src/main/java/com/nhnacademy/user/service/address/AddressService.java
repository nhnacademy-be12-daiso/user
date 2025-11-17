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

package com.nhnacademy.user.service.address;

import com.nhnacademy.user.dto.request.AddressRequest;
import com.nhnacademy.user.dto.response.AddressResponse;
import java.util.List;

public interface AddressService {

    // 새 배송지 추가
    void addAddress(String loginId, AddressRequest request);

    // 모든 주소 목록 조회
    List<AddressResponse> getMyAddresses(String loginId);

    // 특정 주소 정보 수정
    void updateAddress(String loginId, Long addressId, AddressRequest request);

    // 특정 주소 삭제
    void deleteAddress(String loginId, Long addressId);

}

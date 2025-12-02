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

package com.nhnacademy.user.service.account;

import com.nhnacademy.user.dto.request.FindLoginIdRequest;
import com.nhnacademy.user.dto.request.FindPasswordRequest;

public interface FindAccountService {

    // 아이디 찾기
    String findLoginId(FindLoginIdRequest request);

    // 비밀번호 찾기 (임시 비밀번호 발급)
    void createTemporaryPassword(FindPasswordRequest request);

}

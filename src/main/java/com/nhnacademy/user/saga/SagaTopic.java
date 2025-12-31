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

package com.nhnacademy.user.saga;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SagaTopic {

    // exchange는 하나로 돌려씀
    public static final String ORDER_EXCHANGE = "team3.saga.exchange";

    public static final String BOOK_QUEUE = "team3.saga.book.checkout";
    public static final String USER_QUEUE = "team3.saga.user.point-deduct";
    public static final String COUPON_QUEUE = "team3.saga.coupon.use";
    public static final String ORDER_QUEUE = "team3.saga.order.reply"; // 응답용 큐

    public static final String BOOK_RK = "command.book.checkout";
    public static final String USER_RK = "command.user.point-deduct";
    public static final String COUPON_RK = "command.coupon.use";

    public static final String REPLY_RK = "reply.order";


    // ==== 보상 관련 ====

    public static final String BOOK_COMPENSATION_RK = "compensate.book";
    public static final String USER_COMPENSATION_RK = "compensate.user";
    public static final String COUPON_COMPENSATION_RK = "compensate.coupon";
    public static final String REPLY_COMPENSATION_RK = "reply.compensate";

    public static final String BOOK_COMPENSATION_QUEUE = "team3.saga.book.rollback";
    public static final String USER_COMPENSATION_QUEUE = "team3.saga.user.rollback";
    public static final String COUPON_COMPENSATION_QUEUE = "team3.saga.coupon.rollback";
    public static final String ORDER_COMPENSATION_QUEUE = "team3.saga.rollback.reply";

}

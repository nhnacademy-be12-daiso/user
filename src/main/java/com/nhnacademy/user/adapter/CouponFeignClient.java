package com.nhnacademy.user.adapter;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "coupon")
public interface CouponFeignClient {

    // 아까 만든 "시스템 내부용 웰컴 쿠폰 발급 API" 호출
    @PostMapping("/api/user-coupons/welcome/{userId}")
    void issueWelcomeCoupon(@PathVariable("userId") Long userId);
}
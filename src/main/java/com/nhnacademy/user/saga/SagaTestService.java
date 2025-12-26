package com.nhnacademy.user.saga;

import com.nhnacademy.user.exception.point.PointNotEnoughException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 포인트 부족 시나리오를 위한 테스트 서비스
 */
@Slf4j
@Service
public class SagaTestService {
    public void process() {
        throw new PointNotEnoughException("포인트 부족!");
    }
}

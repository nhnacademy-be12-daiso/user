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

package com.nhnacademy.user.exception;

import com.nhnacademy.user.dto.response.ErrorResponse;
import com.nhnacademy.user.exception.account.AccountDormantException;
import com.nhnacademy.user.exception.account.AccountWithdrawnException;
import com.nhnacademy.user.exception.account.NotDormantAccountException;
import com.nhnacademy.user.exception.account.StateNotFoundException;
import com.nhnacademy.user.exception.address.AddressLimitExceededException;
import com.nhnacademy.user.exception.address.AddressNotFoundException;
import com.nhnacademy.user.exception.address.DefaultAddressDeletionException;
import com.nhnacademy.user.exception.address.DefaultAddressRequiredException;
import com.nhnacademy.user.exception.message.InvalidCodeException;
import com.nhnacademy.user.exception.message.MailSendException;
import com.nhnacademy.user.exception.point.InvalidPointInputException;
import com.nhnacademy.user.exception.point.PointNotEnoughException;
import com.nhnacademy.user.exception.point.PointPolicyAlreadyExistsException;
import com.nhnacademy.user.exception.point.PointPolicyNotFoundException;
import com.nhnacademy.user.exception.saga.FailedSerializationException;
import com.nhnacademy.user.exception.user.GradeNotFoundException;
import com.nhnacademy.user.exception.user.PasswordNotMatchException;
import com.nhnacademy.user.exception.user.UserAlreadyExistsException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BAD_REQUEST = "BAD_REQUEST";
    private static final String UNAUTHORIZED = "UNAUTHORIZED";
    private static final String FORBIDDEN = "FORBIDDEN";
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String CONFLICT = "CONFLICT";
    private static final String LOCKED = "LOCKED";

    private static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    // ==================== 400 Bad Request ====================
    @ExceptionHandler(AddressLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handlerAddressLimitExceededException(AddressLimitExceededException ex) {
        // 등록된 주소 10개 초과
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(BAD_REQUEST, 400, ex.getMessage()));
    }

    @ExceptionHandler(PointNotEnoughException.class)
    public ResponseEntity<ErrorResponse> handlerPointNotEnoughException(PointNotEnoughException ex) {
        // 포인트 잔액 부족
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(BAD_REQUEST, 400, ex.getMessage()));
    }

    @ExceptionHandler(PasswordNotMatchException.class)
    public ResponseEntity<ErrorResponse> handlerPasswordNotMatchException(PasswordNotMatchException ex) {
        // 비밀번호 불일치
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(BAD_REQUEST, 400, ex.getMessage()));
    }

    @ExceptionHandler(NotDormantAccountException.class)
    public ResponseEntity<ErrorResponse> handlerNotDormantAccountException(NotDormantAccountException ex) {
        // 휴면 상태가 아닌 계정
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(BAD_REQUEST, 400, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handlerMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        // @Valid 유효성 검사 실패
        // 에러가 발생한 필드명과 메시지를 Map으로 구현
        Map<String, String> errorMap = new HashMap<>();

        var fieldError = ex.getBindingResult().getFieldError();

        if (fieldError != null) {
            errorMap.put("field", fieldError.getField());
            errorMap.put("message", fieldError.getDefaultMessage());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorMap);
    }

    @ExceptionHandler(DefaultAddressDeletionException.class)
    public ResponseEntity<ErrorResponse> handlerDefaultAddressDeletionException(DefaultAddressDeletionException ex) {
        // 기본 배송지 삭제
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(BAD_REQUEST, 400, ex.getMessage()));
    }

    @ExceptionHandler(DefaultAddressRequiredException.class)
    public ResponseEntity<Map<String, String>> handlerDefaultAddressRequiredException(
            DefaultAddressRequiredException ex) {
        // 기본 배송지 설정 해제 불가
        Map<String, String> errorMap = new HashMap<>();

        errorMap.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorMap);
    }

    @ExceptionHandler(InvalidPointInputException.class)
    public ResponseEntity<ErrorResponse> handlerInvalidPointInputException(InvalidPointInputException ex) {
        // 잘못된 포인트 입력값
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(BAD_REQUEST, 400, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        // 헤더 타입 불일치
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(BAD_REQUEST, 400, ex.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        // 필수 헤더 누락
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(BAD_REQUEST, 400, ex.getHeaderName()));
    }

    // ==================== 401 Unauthorized ====================
    @ExceptionHandler(InvalidCodeException.class)
    public ResponseEntity<ErrorResponse> handlerInvalidCodeException(InvalidCodeException ex) {
        // 올바르지 않은 코드 입력
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(UNAUTHORIZED, 401, ex.getMessage()));
    }

    // ==================== 403 Forbidden ====================
    @ExceptionHandler(AccountWithdrawnException.class)
    public ResponseEntity<ErrorResponse> handlerAccountWithdrawnException(AccountWithdrawnException ex) {
        // 탈퇴한 계정
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(FORBIDDEN, 403, ex.getMessage()));
    }

    // ==================== 404 Not Found ====================
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerUserNotFoundException(UserNotFoundException ex) {
        // 찾을 수 없는 유저
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(NOT_FOUND, 404, ex.getMessage()));
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerAddressNotFoundException(AddressNotFoundException ex) {
        // 찾을 수 없는 주소
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(NOT_FOUND, 404, ex.getMessage()));
    }

    @ExceptionHandler(PointPolicyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerPointPolicyNotFoundException(PointPolicyNotFoundException ex) {
        // 찾을 수 없는 포인트 정책
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(NOT_FOUND, 404, ex.getMessage()));
    }

    @ExceptionHandler(StateNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerStateNotFoundException(StateNotFoundException ex) {
        // 찾을 수 없는 상태 정보
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(NOT_FOUND, 404, ex.getMessage()));
    }

    @ExceptionHandler(GradeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerGradeNotFoundException(GradeNotFoundException ex) {
        // 찾을 수 없는 등급 정보
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(NOT_FOUND, 404, ex.getMessage()));
    }

    // ==================== 409 Conflict ====================
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlerUserAlreadyExistsException(UserAlreadyExistsException ex) {
        // 이미 존재하는 유저
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(CONFLICT, 409, ex.getMessage()));
    }

    @ExceptionHandler(PointPolicyAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlerUserPointPolicyAlreadyExistsException(
            PointPolicyAlreadyExistsException ex) {
        // 이미 존재하는 정책
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(CONFLICT, 409, ex.getMessage()));
    }

    // ==================== 423 Locked ====================
    @ExceptionHandler(AccountDormantException.class)
    public ResponseEntity<ErrorResponse> handlerAccountDormantException(AccountDormantException ex) {
        // 휴면 상태인 계정
        return ResponseEntity
                .status(HttpStatus.LOCKED)
                .body(new ErrorResponse(LOCKED, 423, ex.getMessage()));
    }

    // ==================== 500 Internal Server Error ====================
    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<ErrorResponse> handlerMailSendException(MailSendException ex) {
        // 메일 발송 중 오류
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(INTERNAL_SERVER_ERROR, 500, ex.getMessage()));
    }

    @ExceptionHandler(FailedSerializationException.class)
    public ResponseEntity<ErrorResponse> handlerFailedSerializationException(FailedSerializationException ex) {
        // saga 통신 중 직렬화 오류
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(INTERNAL_SERVER_ERROR, 500, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        // 정의되지 않은 모든 예외 처리
        log.error("처리되지 않은 예외: ", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(INTERNAL_SERVER_ERROR, 500, "서버 내부 오류가 발생했습니다."));
    }

}

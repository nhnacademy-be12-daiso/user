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
import com.nhnacademy.user.exception.address.AddressLimitExceededException;
import com.nhnacademy.user.exception.address.AddressNotFoundException;
import com.nhnacademy.user.exception.address.DefaultAddressDeletionException;
import com.nhnacademy.user.exception.message.InvalidCodeException;
import com.nhnacademy.user.exception.point.PointNotEnoughException;
import com.nhnacademy.user.exception.point.PointPolicyAlreadyExistsException;
import com.nhnacademy.user.exception.point.PointPolicyNotFoundException;
import com.nhnacademy.user.exception.user.NotDormantUserException;
import com.nhnacademy.user.exception.user.PasswordNotMatchException;
import com.nhnacademy.user.exception.user.UserAlreadyExistsException;
import com.nhnacademy.user.exception.user.UserDormantException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.exception.user.UserWithdrawnException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 Bad Request
    @ExceptionHandler(AddressLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handlerAddressLimitExceededException(AddressLimitExceededException ex) {
        // 등록된 주소 10개 초과
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", 400, ex.getMessage()));
    }

    @ExceptionHandler(PointNotEnoughException.class)
    public ResponseEntity<ErrorResponse> handlerPointNotEnoughException(PointNotEnoughException ex) {
        // 포인트 잔액 부족
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", 400, ex.getMessage()));
    }

    @ExceptionHandler(PasswordNotMatchException.class)
    public ResponseEntity<ErrorResponse> handlerPasswordNotMatchException(PasswordNotMatchException ex) {
        // 비밀번호 불일치
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", 400, ex.getMessage()));
    }

    @ExceptionHandler(NotDormantUserException.class)
    public ResponseEntity<ErrorResponse> handlerNotDormantUserException(NotDormantUserException ex) {
        // 휴면 상태가 아닌 회원
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", 400, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handlerMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        // @Valid 유효성 검사 실패
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", 400, ex.getMessage()));
    }

    @ExceptionHandler(DefaultAddressDeletionException.class)
    public ResponseEntity<ErrorResponse> handlerDefaultAddressDeletionException(DefaultAddressDeletionException ex) {
        // 기본 배송지 삭제
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", 400, ex.getMessage()));
    }

    // 401 Unauthorized
    @ExceptionHandler(InvalidCodeException.class)
    public ResponseEntity<ErrorResponse> handlerInvalidCodeException(InvalidCodeException ex) {
        // 올바르지 않은 코드 입력
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("UNAUTHORIZED", 401, ex.getMessage()));
    }

    // 403 Forbidden
    @ExceptionHandler(UserWithdrawnException.class)
    public ResponseEntity<ErrorResponse> handlerUserWithdrawnException(UserWithdrawnException ex) {
        // 탈퇴한 회원
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("FORBIDDEN", 403, ex.getMessage()));
    }

    // 404 Not Found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerUserNotFoundException(UserNotFoundException ex) {
        // 찾을 수 없는 유저
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", 404, ex.getMessage()));
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerAddressNotFoundException(AddressNotFoundException ex) {
        // 찾을 수 없는 주소
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", 404, ex.getMessage()));
    }

    @ExceptionHandler(PointPolicyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerPointPolicyNotFoundException(PointPolicyNotFoundException ex) {
        // 존재하지 않는 포인트 정책
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", 404, ex.getMessage()));
    }

    // 409 Conflict
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlerUserAlreadyExistsException(UserAlreadyExistsException ex) {
        // 이미 존재하는 유저
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICT", 409, ex.getMessage()));
    }

    @ExceptionHandler(PointPolicyAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlerUserPointPolicyAlreadyExistsException(
            PointPolicyAlreadyExistsException ex) {
        // 이미 존재하는 정책
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICT", 409, ex.getMessage()));
    }

    // 423 Locked
    @ExceptionHandler(UserDormantException.class)
    public ResponseEntity<ErrorResponse> handlerUserDormantException(UserDormantException ex) {
        // 휴면 상태인 유저
        return ResponseEntity
                .status(HttpStatus.LOCKED)
                .body(new ErrorResponse("LOCKED", 423, ex.getMessage()));
    }

}

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
import com.nhnacademy.user.exception.user.UserAlreadyExistsException;
import com.nhnacademy.user.exception.user.UserDormantException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
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
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse("BAD_REQUEST", 400, ex.getMessage()));
    }

    // 401 Unauthorized
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handlerAuthenticationException(AuthenticationException ex) {
        // 로그인 실패 (ID/PW 불일치)
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse("UNAUTHORIZED", 401, "아이디 또는 비밀번호가 일치하지 않습니다."));
    }

    // 404 Not Found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerUserNotFoundException(UserNotFoundException ex) {
        // 찾을 수 없는 유저
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse("NOT_FOUND", 404, ex.getMessage()));
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerAddressNotFoundException(AddressNotFoundException ex) {
        // 찾을 수 없는 주소
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse("NOT_FOUND", 404, ex.getMessage()));
    }

    // 409 Conflict
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlerUserAlreadyExistsException(UserAlreadyExistsException ex) {
        // 이미 존재하는 유저
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse("CONFLICT", 409, ex.getMessage()));
    }

    // 423 Locked
    @ExceptionHandler(UserDormantException.class)
    public ResponseEntity<ErrorResponse> handlerUserDormantException(UserDormantException ex) {
        // 휴면 상태인 유저
        return ResponseEntity
                .status(HttpStatus.LOCKED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse("LOCKED", 423, ex.getMessage()));
    }

}

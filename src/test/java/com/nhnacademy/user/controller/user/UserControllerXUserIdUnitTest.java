package com.nhnacademy.user.controller.user;

import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserControllerXUserIdUnitTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Test
    @DisplayName("X-User-Id로 내 정보 조회 성공")
    void getMyInfo_WithXUserId_Success() {
        // given
        Long userCreatedId = 29L;
        UserResponse expectedResponse = new UserResponse(
                "testuser",
                "테스트유저",
                "01012345678",
                "test@example.com",
                LocalDate.of(1990, 1, 1),
                "GENERAL",
                BigDecimal.valueOf(1000),
                "ACTIVE",
                LocalDateTime.now()
        );

        given(userService.getUserInfo(userCreatedId)).willReturn(expectedResponse);

        // when
        ResponseEntity<UserResponse> response = userController.getMyInfo(userCreatedId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().loginId()).isEqualTo("testuser");
        assertThat(response.getBody().userName()).isEqualTo("테스트유저");
        assertThat(response.getBody().email()).isEqualTo("test@example.com");

        verify(userService).getUserInfo(userCreatedId);
    }

    @Test
    @DisplayName("다른 X-User-Id로 조회 시 다른 사용자 정보 반환")
    void getMyInfo_DifferentUserIds() {
        // given
        Long userId1 = 10L;
        Long userId2 = 20L;

        UserResponse user1 = new UserResponse(
                "user1", "사용자1", "01011111111", "user1@test.com",
                LocalDate.of(1990, 1, 1), "GENERAL", BigDecimal.ZERO, "ACTIVE", LocalDateTime.now()
        );

        UserResponse user2 = new UserResponse(
                "user2", "사용자2", "01022222222", "user2@test.com",
                LocalDate.of(1991, 2, 2), "VIP", BigDecimal.valueOf(5000), "ACTIVE", LocalDateTime.now()
        );

        given(userService.getUserInfo(userId1)).willReturn(user1);
        given(userService.getUserInfo(userId2)).willReturn(user2);

        // when
        ResponseEntity<UserResponse> response1 = userController.getMyInfo(userId1);
        ResponseEntity<UserResponse> response2 = userController.getMyInfo(userId2);

        // then
        assertThat(response1.getBody()).isNotNull();
        assertThat(response1.getBody().loginId()).isEqualTo("user1");
        assertThat(response1.getBody().userName()).isEqualTo("사용자1");

        assertThat(response2.getBody()).isNotNull();
        assertThat(response2.getBody().loginId()).isEqualTo("user2");
        assertThat(response2.getBody().userName()).isEqualTo("사용자2");
        assertThat(response2.getBody().gradeName()).isEqualTo("VIP");
    }

    @Test
    @DisplayName("X-User-Id가 Long 타입으로 정상 전달됨")
    void xUserIdIsParsedAsLong() {
        // given
        Long largeUserId = 99999L;
        UserResponse response = new UserResponse(
                "testuser", "테스트", "01012345678", "test@test.com",
                LocalDate.of(1990, 1, 1), "GENERAL", BigDecimal.ZERO, "ACTIVE", LocalDateTime.now()
        );

        given(userService.getUserInfo(largeUserId)).willReturn(response);

        // when
        ResponseEntity<UserResponse> result = userController.getMyInfo(largeUserId);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userService).getUserInfo(largeUserId);
    }

    @Test
    @DisplayName("여러 사용자 ID로 순차적 조회")
    void getMyInfo_MultipleSequentialCalls() {
        // given
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long userId3 = 3L;

        UserResponse response1 = createUserResponse("user1", "유저1");
        UserResponse response2 = createUserResponse("user2", "유저2");
        UserResponse response3 = createUserResponse("user3", "유저3");

        given(userService.getUserInfo(userId1)).willReturn(response1);
        given(userService.getUserInfo(userId2)).willReturn(response2);
        given(userService.getUserInfo(userId3)).willReturn(response3);

        // when
        ResponseEntity<UserResponse> result1 = userController.getMyInfo(userId1);
        ResponseEntity<UserResponse> result2 = userController.getMyInfo(userId2);
        ResponseEntity<UserResponse> result3 = userController.getMyInfo(userId3);

        // then
        assertThat(result1.getBody().loginId()).isEqualTo("user1");
        assertThat(result2.getBody().loginId()).isEqualTo("user2");
        assertThat(result3.getBody().loginId()).isEqualTo("user3");
    }

    private UserResponse createUserResponse(String loginId, String userName) {
        return new UserResponse(
                loginId, userName, "01012345678", loginId + "@test.com",
                LocalDate.of(1990, 1, 1), "GENERAL", BigDecimal.ZERO, "ACTIVE", LocalDateTime.now()
        );
    }
}


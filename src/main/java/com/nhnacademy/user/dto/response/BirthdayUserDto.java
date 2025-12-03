package com.nhnacademy.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BirthdayUserDto {
    private Long userCreatedId;
    private String username; // 로그용
    private LocalDate birth; // 설계용
}

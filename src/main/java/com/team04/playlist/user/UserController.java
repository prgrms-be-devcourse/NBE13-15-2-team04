package com.team04.playlist.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public void getUserInfo(){
        throw new UnsupportedOperationException("유저정보 가져오기 구현 예정");
    }

    @PatchMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public void updateNickname(){
        throw new UnsupportedOperationException("닉네임 변경 기능 구현 예정");
    }
}

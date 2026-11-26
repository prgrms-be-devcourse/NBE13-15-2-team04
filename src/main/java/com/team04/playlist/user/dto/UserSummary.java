package com.team04.playlist.user.dto;

import com.team04.playlist.user.entity.User;

public record UserSummary(
        Long userId,
        String nickname
) {
    public static UserSummary from(User user){
        return new UserSummary(user.getId(), user.getNickname());
    }
}

package com.team04.playlist.playlist.dto;

public record PlaylistSuccessResponse<T>(int status, String message, T data) {

    public static <T> PlaylistSuccessResponse<T> ok(String message, T data) {
        return new PlaylistSuccessResponse<>(200, message, data);
    }

    public static <T> PlaylistSuccessResponse<T> created(String message, T data) {
        return new PlaylistSuccessResponse<>(201, message, data);
    }
}
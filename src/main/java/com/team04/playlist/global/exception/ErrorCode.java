package com.team04.playlist.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 BAD_REQUEST
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INVALID_POSITION(HttpStatus.BAD_REQUEST, "순번이 유효한 범위를 벗어났습니다."),
    DEFAULT_LIBRARY_NOT_DELETABLE(HttpStatus.BAD_REQUEST, "기본 보관함은 삭제할 수 없습니다."),

    // 401 UNAUTHORIZED
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),

    // 403 FORBIDDEN
    PRIVATE_PLAYLIST(HttpStatus.FORBIDDEN, "비공개 플레이리스트입니다."),
    NOT_PLAYLIST_OWNER(HttpStatus.FORBIDDEN, "플레이리스트 소유자만 가능한 작업입니다."),
    NOT_PLAYLIST_EDITOR(HttpStatus.FORBIDDEN, "플레이리스트를 편집할 권한이 없습니다."),
    NOT_LIBRARY_OWNER(HttpStatus.FORBIDDEN, "보관함에 접근할 권한이 없습니다."),

    // 404 NOT_FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "플레이리스트를 찾을 수 없습니다."),
    TRACK_NOT_FOUND(HttpStatus.NOT_FOUND, "곡을 찾을 수 없습니다."),
    TRACK_NOT_IN_PLAYLIST(HttpStatus.NOT_FOUND, "플레이리스트에 담기지 않은 곡입니다."),
    COLLABORATOR_NOT_FOUND(HttpStatus.NOT_FOUND, "편집자를 찾을 수 없습니다."),
    LIBRARY_NOT_FOUND(HttpStatus.NOT_FOUND, "보관함을 찾을 수 없습니다."),

    // 409 CONFLICT
    TRACK_ALREADY_IN_PLAYLIST(HttpStatus.CONFLICT, "이미 플레이리스트에 담긴 곡입니다."),
    ALREADY_COLLABORATOR(HttpStatus.CONFLICT, "이미 편집자이거나 초대할 수 없는 사용자입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    DUPLICATE_LIBRARY_NAME(HttpStatus.CONFLICT, "같은 이름의 보관함이 이미 존재합니다."),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "다른 사용자의 변경과 충돌했습니다. 다시 시도해 주세요."),

    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 502 BAD_GATEWAY
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 호출에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}

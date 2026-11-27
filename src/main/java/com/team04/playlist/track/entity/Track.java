package com.team04.playlist.track.entity;

import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // iTunes trackId
    @Column(nullable = false, unique = true)
    private Long externalId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 200)
    private String artist;

    @Column(length = 200)
    private String album;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String previewUrl;

    // 곡 길이 (밀리초)
    private Integer durationMs;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Track(Long externalId, String title, String artist, String album,
                  String imageUrl, String previewUrl, Integer durationMs) {
        this.externalId = externalId;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.imageUrl = imageUrl;
        this.previewUrl = previewUrl;
        this.durationMs = durationMs;
    }

    // 필수 값을 검증하고 Track을 생성
    public static Track create(Long externalId, String title, String artist, String album,
                               String imageUrl, String previewUrl, Integer durationMs) {
        if (externalId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "externalId는 필수입니다.");
        }
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "곡 제목은 필수입니다.");
        }
        if (artist == null || artist.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "가수 이름은 필수입니다.");
        }
        return new Track(externalId, title, artist, album, imageUrl, previewUrl, durationMs);
    }
}

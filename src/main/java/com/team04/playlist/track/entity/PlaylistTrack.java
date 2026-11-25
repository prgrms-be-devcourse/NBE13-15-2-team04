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
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_playlist_track", columnNames = {"playlist_id", "track_id"}),
        @UniqueConstraint(name = "uk_playlist_position", columnNames = {"playlist_id", "position"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaylistTrack {

    // 정렬 키 최대 길이 (DB 컬럼 VARCHAR(64))
    public static final int MAX_POSITION_LENGTH = 64;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 플레이리스트 id (다른 도메인이라 id로만 참조)
    @Column(nullable = false)
    private Long playlistId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    // 정렬 키 (a0, a0v, a1 ...)
    @Column(nullable = false, columnDefinition = "VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin")
    private String position;

    // 곡을 추가한 사용자 id
    @Column(nullable = false)
    private Long addedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private PlaylistTrack(Long playlistId, Track track, String position, Long addedBy) {
        this.playlistId = playlistId;
        this.track = track;
        this.position = position;
        this.addedBy = addedBy;
    }

    // 필수 값을 검증하고 플레이리스트에 담긴 곡을 생성
    public static PlaylistTrack create(Long playlistId, Track track, String position, Long addedBy) {
        if (playlistId == null || track == null || addedBy == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "플레이리스트, 곡, 추가한 사용자는 필수입니다.");
        }
        validatePosition(position);
        return new PlaylistTrack(playlistId, track, position, addedBy);
    }

    // 정렬 키를 바꿔 순서를 옮김
    public void moveTo(String newPosition) {
        validatePosition(newPosition);
        this.position = newPosition;
    }

    private static void validatePosition(String position) {
        if (position == null || position.isBlank() || position.length() > MAX_POSITION_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_POSITION);
        }
    }
}

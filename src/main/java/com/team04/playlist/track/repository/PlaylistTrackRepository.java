package com.team04.playlist.track.repository;

import com.team04.playlist.track.entity.PlaylistTrack;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {

    // 같은 곡이 이미 담겼는지 확인
    boolean existsByPlaylistIdAndTrackId(Long playlistId, Long trackId);

    // 플레이리스트에 담긴 특정 곡 조회 (삭제, 순서 변경)
    Optional<PlaylistTrack> findByPlaylistIdAndTrackId(Long playlistId, Long trackId);

    // 플레이리스트의 곡 목록을 순서대로 조회 (곡 정보까지 한 번에 가져옴)
    @EntityGraph(attributePaths = "track")
    List<PlaylistTrack> findAllByPlaylistIdOrderByPositionAsc(Long playlistId);

    // 마지막 순서의 곡 조회 (맨 뒤에 추가할 때)
    Optional<PlaylistTrack> findTopByPlaylistIdOrderByPositionDesc(Long playlistId);
}

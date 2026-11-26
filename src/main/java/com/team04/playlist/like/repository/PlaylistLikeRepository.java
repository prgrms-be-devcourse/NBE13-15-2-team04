package com.team04.playlist.like.repository;

import com.team04.playlist.like.entity.PlaylistLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaylistLikeRepository extends JpaRepository<PlaylistLike, Long> {

    boolean existsByUserIdAndPlaylistId(Long userId, Long playlistId);

    //좋아요 수. 숫자만 조회하므로 영속성 컨텍스트를 거치지 않고 항상 DB 최신 값
    long countByPlaylistId(Long playlistId);

    //동시에 두 번 눌러도 안전한 등록. 유니크(user_id, playlist_id) 중복이면 예외 없이 무시한다.
    //반환값: 1 = 새로 등록, 0 = 이미 좋아요 상태
    @Modifying
    @Query(value = "INSERT IGNORE INTO playlist_like (user_id, playlist_id) VALUES (:userId, :playlistId)",
            nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("playlistId") Long playlistId);

    //동시에 두 번 취소해도 한쪽만 1을 받는 원자적 삭제 (파생 삭제는 조회 후 삭제라 동시 요청에 취약)
    //반환값: 1 = 삭제됨, 0 = 원래 없었음
    @Modifying
    @Query("DELETE FROM PlaylistLike pl WHERE pl.userId = :userId AND pl.playlistId = :playlistId")
    int deleteByUserIdAndPlaylistId(@Param("userId") Long userId, @Param("playlistId") Long playlistId);
}

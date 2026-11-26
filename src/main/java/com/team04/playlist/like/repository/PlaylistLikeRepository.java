package com.team04.playlist.like.repository;

import com.team04.playlist.like.entity.PlaylistLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaylistLikeRepository extends JpaRepository<PlaylistLike, Long> {

    boolean existsByUserIdAndPlaylistId(Long userId, Long playlistId);

    //좋아요 중복 클릭될 때, 예외 없이 무시 -> 동시성 문제 해결
    @Modifying
    @Query(value = "INSERT IGNORE INTO playlist_like (user_id, playlist_id) VALUES (:userId, :playlistId)",
            nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("playlistId") Long playlistId);

    @Modifying
    @Query("DELETE FROM PlaylistLike pl WHERE pl.userId = :userId AND pl.playlistId = :playlistId")
    int deleteByUserIdAndPlaylistId(@Param("userId") Long userId, @Param("playlistId") Long playlistId);
}

package com.team04.playlist.playlist.repository;

import com.team04.playlist.playlist.entity.Playlist;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // 삭제되지 않은 플레이리스트만 찾음. 삭제된 것은 없는것으로 취급
    Optional<Playlist> findByIdAndDeletedAtIsNull(Long id);

    // 공개 목록 조회. owner 조인으로 닉네임 N+1 방지
    @EntityGraph(attributePaths = "owner")
    Page<Playlist> findByIsPublicTrueAndDeletedAtIsNull(Pageable pageable);

    @Modifying
    @Query("update Playlist p set p.likeCount = p.likeCount + 1 where p.id = :id and p.deletedAt is null")
    void increaseLikeCount(@Param("id") Long id);

    @Modifying
    @Query("update Playlist p set p.likeCount = p.likeCount - 1 "
            + "where p.id = :id and p.deletedAt is null and p.likeCount > 0")
    void decreaseLikeCount(@Param("id") Long id);

    @Query("select p.likeCount from Playlist p where p.id = :id and p.deletedAt is null")
    Optional<Integer> findLikeCount(@Param("id") Long id);
}
package com.team04.playlist.playlist.repository;

import com.team04.playlist.playlist.entity.Playlist;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // 삭제되지 않은 플레이리스트만 찾음. 삭제된 것은 없는것으로 취급
    Optional<Playlist> findByIdAndDeletedAtIsNull(Long id);

    // 공개 목록 조회. owner 조인으로 닉네임 N+1 방지
    @EntityGraph(attributePaths = "owner")
    Page<Playlist> findByIsPublicTrueAndDeletedAtIsNull(Pageable pageable);
}
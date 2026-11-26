package com.team04.playlist.playlist.repository;

import com.team04.playlist.playlist.entity.Playlist;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // 삭제되지 않은 플레이리스트만 찾음. 삭제된 것은 없는것으로 취급
    Optional<Playlist> findByIdAndDeletedAtIsNull(Long id);
}
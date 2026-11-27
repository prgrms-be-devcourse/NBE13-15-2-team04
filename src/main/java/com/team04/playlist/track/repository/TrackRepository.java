package com.team04.playlist.track.repository;

import com.team04.playlist.track.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {

    // iTunes trackId로 이미 저장된 곡 조회
    Optional<Track> findByExternalId(Long externalId);
}

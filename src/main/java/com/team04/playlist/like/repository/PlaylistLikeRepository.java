package com.team04.playlist.like.repository;

import com.team04.playlist.like.entity.PlaylistLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistLikeRepository extends JpaRepository<PlaylistLike, Long>{

    public boolean existsByUserIdAndPlaylistId(Long userId, Long PlaylistId);

    public int deleteByUserIdAndPlaylistId(Long userId, Long PlaylistId);
}
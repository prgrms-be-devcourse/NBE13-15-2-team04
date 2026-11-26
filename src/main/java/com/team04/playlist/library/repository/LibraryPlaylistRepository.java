package com.team04.playlist.library.repository;

import com.team04.playlist.library.entity.LibraryPlaylist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LibraryPlaylistRepository extends JpaRepository<LibraryPlaylist,Long> {

    public boolean existsByLibraryIdAndPlaylistId(Long libraryId, Long playlistId);

    //totalElements, hasNext 필요하니 Page로 받는다
    Page<LibraryPlaylist> findAllByLibraryId(Long libraryId, Pageable pageable);

    //db에 있는 행을 삭제해야, 사용자에게 안 보여지게 된다.
    //빼기는 library_playlist의 연결 행만 지우고, 플레이리스트(playlist)와 담긴 곡(playlist_track)은 그대로 남는다.
    @Modifying
    @Query("DELETE FROM LibraryPlaylist lp WHERE lp.libraryId = :libraryId AND lp.playlistId = :playlistId")
    int deleteByLibraryIdAndPlaylistId(@Param("libraryId") Long libraryId,
                                       @Param("playlistId") Long playlistId);
}

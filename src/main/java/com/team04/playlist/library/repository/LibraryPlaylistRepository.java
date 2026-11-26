package com.team04.playlist.library.repository;

import com.team04.playlist.library.entity.LibraryPlaylist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryPlaylistRepository extends JpaRepository<LibraryPlaylist,Long> {

    public boolean existsByLibraryIdAndPlaylistId(Long libraryId, Long playlistId);

    //totalElements, hasNext 필요하니 Page로 받는다
    Page<LibraryPlaylist> findAllByLibraryId(Long libraryId, Pageable pageable);

    long deleteByLibraryIdAndPlaylistId(Long libraryId, Long playlistId);
}

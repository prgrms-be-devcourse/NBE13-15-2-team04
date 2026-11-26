package com.team04.playlist.library.repository;

import com.team04.playlist.library.entity.Library;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibraryRepository extends JpaRepository<Library,Long> {
    List<Library> findAllByUserId(Long userId);
}

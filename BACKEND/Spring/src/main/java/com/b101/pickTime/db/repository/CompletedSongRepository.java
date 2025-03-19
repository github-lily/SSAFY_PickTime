package com.b101.pickTime.db.repository;

import com.b101.pickTime.db.entity.CompletedSong;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletedSongRepository extends JpaRepository<CompletedSong, Integer> {
}

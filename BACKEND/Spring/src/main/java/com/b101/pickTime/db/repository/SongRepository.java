package com.b101.pickTime.db.repository;

import com.b101.pickTime.db.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Integer> {
}

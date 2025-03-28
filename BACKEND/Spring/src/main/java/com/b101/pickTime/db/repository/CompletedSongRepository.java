package com.b101.pickTime.db.repository;

import com.b101.pickTime.api.completedactivities.response.CompletedActivitiesResDto;
import com.b101.pickTime.db.entity.CompletedSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompletedSongRepository extends JpaRepository<CompletedSong, Integer> {

    @Query("SELECT new com.b101.pickTime.api.completedactivities.response.CompletedActivitiesResDto(" +
            "c.createdAt, COUNT(c)) " +
            "FROM CompletedSong c " +
            "WHERE c.user.userId = :userId " +
            "GROUP BY c.createdAt")
    List<CompletedActivitiesResDto> countCompletedStepsGroupedByCreatedAt(@Param("userId") Integer userId);

    Optional<CompletedSong> findByUserUserIdAndSongSongId(Integer userId, Integer songId);

    @Query("SELECT cs.score FROM CompletedSong cs WHERE cs.user.userId = :userId AND cs.song.songId = :songId")
    Integer findScoreByUserIdAndSongId(@Param("userId") Integer userId, @Param("songId") Integer songId);
}

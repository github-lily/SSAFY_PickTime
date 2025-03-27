package com.b101.pickTime.api.completedsong.service.impl;

import com.b101.pickTime.api.completedactivities.response.CompletedActivitiesResDto;
import com.b101.pickTime.api.completedsong.service.CompletedSongService;
import com.b101.pickTime.db.entity.CompletedSong;
import com.b101.pickTime.db.entity.Song;
import com.b101.pickTime.db.entity.User;
import com.b101.pickTime.db.repository.CompletedSongRepository;
import com.b101.pickTime.db.repository.SongRepository;
import com.b101.pickTime.db.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompletedSongServiceImpl implements CompletedSongService {

    private final CompletedSongRepository completedSongRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    @Override
    public void completeSong(Integer userId, Integer songId, Integer score) {

        Optional<CompletedSong> completedSong = completedSongRepository.findByUserUserIdAndSongSongId(userId, songId);

        if(completedSong.isPresent()) {
            CompletedSong playedSong = completedSong.get();
            playedSong.setScore(Math.max(playedSong.getScore(), score));
            completedSongRepository.save(playedSong);
        }
        else{
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("찾는 회원이 없습니다."));

            Song song = songRepository.findById(songId)
                    .orElseThrow(() -> new EntityNotFoundException("찾는 곡이 없습니다."));


            completedSongRepository.save(
                            CompletedSong.builder()
                                    .user(user)
                                    .song(song)
                                    .score(score)
                                    .build()
                            );
        }

    }

    @Override
    public List<CompletedActivitiesResDto> getPickDaysOfSong(Integer userId) {
        return completedSongRepository.countCompletedStepsGroupedByCreatedAt(userId);
    }
}

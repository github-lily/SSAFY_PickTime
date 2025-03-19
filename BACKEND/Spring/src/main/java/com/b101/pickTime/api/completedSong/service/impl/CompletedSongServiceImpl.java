package com.b101.pickTime.api.completedSong.service.impl;

import com.b101.pickTime.api.completedSong.service.CompletedSongService;
import com.b101.pickTime.db.entity.CompletedSong;
import com.b101.pickTime.db.entity.CompletedStep;
import com.b101.pickTime.db.entity.Song;
import com.b101.pickTime.db.entity.User;
import com.b101.pickTime.db.repository.CompletedSongRepository;
import com.b101.pickTime.db.repository.SongRepository;
import com.b101.pickTime.db.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompletedSongServiceImpl implements CompletedSongService {

    private final CompletedSongRepository completedSongRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    @Override
    public void completeSong(Integer userId, Integer songId, Integer score) {

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

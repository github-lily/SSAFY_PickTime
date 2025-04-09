package com.b101.pickTime.api.game.service.impl;

import com.b101.pickTime.api.game.response.SongDataResDto;
import com.b101.pickTime.api.game.response.SongGameResDto;
import com.b101.pickTime.api.game.service.SongDataService;
import com.b101.pickTime.db.document.SongData;
import com.b101.pickTime.db.entity.CompletedSong;
import com.b101.pickTime.db.repository.CompletedSongRepository;
import com.b101.pickTime.db.repository.SongDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SongDataServiceImpl implements SongDataService {

    private final SongDataRepository songDataRepository;
    private final CompletedSongRepository completedSongRepository;

    @Override
    public List<SongDataResDto> getAllSongs(int userId){
        List<SongDataResDto> songs = new ArrayList<>();
        List<SongData> songDatas = songDataRepository.findAll();

        for(SongData songData : songDatas){
            SongDataResDto song = songDataToDto(songData);
            CompletedSong cs = completedSongRepository.findTopScoreByUserUserIdAndSongSongIdOrderByScoreDesc(userId, songData.getSongDataId());
            Integer star = cs != null ? cs.getScore() : null;
            song.setStar(star == null ? 0 : star);

            songs.add(song);
        }

        return songs;

    }

    @Override
    public SongGameResDto getSong(Integer songId) {
        return songDataRepository.findSongGameResDtoBySongDataId(songId);
    }

    private SongDataResDto songDataToDto(SongData songData){

        return SongDataResDto.builder()
                .songId(songData.getSongDataId())
                .title(songData.getTitle())
                .bpm(songData.getBpm())
                .songUri(songData.getSongUri())
                .songThumbnailUri(songData.getSongThumbnailUri())
                .chords(getChordList(songData.getChordProgression()))
                .build();

    }

    private List<String> getChordList(List<SongData.Measure> chordProgression){
        Set<String> chords = new HashSet<>();

        for(SongData.Measure measure : chordProgression){
            chords.addAll(measure.getChordBlocks());
        }

        chords.remove("X");

        return chords.stream().toList();
    }

}

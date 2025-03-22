package com.b101.pickTime.api.songdata.service.impl;

import com.b101.pickTime.api.songdata.response.SongDataResDto;
import com.b101.pickTime.api.songdata.service.SongDataService;
import com.b101.pickTime.db.document.SongData;
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

    @Override
    public List<SongDataResDto> findAllSongs(){
        List<SongDataResDto> songs = new ArrayList<>();

        List<SongData> songDatas = songDataRepository.findAll();

        for(SongData songData : songDatas){
            songs.add(songDataToDto(songData));
        }

        return songs;

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
            for(SongData.ChordBlock chordBlock : measure.getChordBlocks()){
                chords.add(chordBlock.getName());
            }
        }

        return chords.stream().toList();
    }

}

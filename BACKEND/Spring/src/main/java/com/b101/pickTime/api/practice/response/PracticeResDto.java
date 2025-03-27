package com.b101.pickTime.api.practice.response;

import com.b101.pickTime.api.chord.response.ChordDataResDto;
import com.b101.pickTime.api.game.response.SongGameResDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PracticeResDto {

    List<ChordDataResDto> chords;
    SongGameResDto song;

}

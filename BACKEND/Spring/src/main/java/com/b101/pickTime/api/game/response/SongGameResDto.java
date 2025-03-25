package com.b101.pickTime.api.game.response;

import com.b101.pickTime.db.document.SongData;
import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class SongGameResDto {

    private String title;
    private int bpm;
    private String timeSignature;
    private List<SongData.Measure> chordProgression;
    private String songUri;

}

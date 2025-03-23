package com.b101.pickTime.api.chord.response;

import com.b101.pickTime.db.document.ChordData;
import lombok.*;

@Getter @Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ChordDataResDto {

    private String chordName;
    private String chordImageUri;
    private String chordSoundUri;
    private ChordData.ChordFingering chordFingering;

}

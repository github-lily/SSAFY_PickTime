package com.b101.pickTime.db.document;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter @Setter
@Document(collection = "chord_data")
public class ChordData {

    @Id
    @Field("_id")
    private Integer chordId;
    private String chordName;
    private ChordFingering chordFingering;
    private String chordSoundUri;
    private String chordImageUri;


    @Getter @Setter
    @AllArgsConstructor
    @ToString
    @NoArgsConstructor
    public static class ChordFingering {

        private List<Position> positions;
        private List<Integer> openStrings;
        private List<Integer> muteStrings;
    }
    @Getter @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private int finger;
        private int fret;
        private List<Integer> strings;
    }

}

package com.b101.pickTime.db.document;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter @Setter
@ToString
@Document(collection = "song_data")
public class SongData {

    @Id
    @Field("_id")
    private Integer songDataId;

    private String title;
    private String artist;
    private Integer bpm;
    private String timeSignature;           // 예: "4/4"
    private String songUri;                 // 오디오 파일 경로
    private String songThumbnailUri;        // 썸네일 이미지 경로
    private Integer durationSec;            // 총 재생 시간(초)

    private List<Measure> chordProgression;

    @Getter @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Measure {
        private int measureIndex;
        private List<String> chordBlocks;
    }

//    @Getter @Setter
//    @ToString
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class ChordBlock {
//        private String name;         // 예: "C", "Am"
//        private int durationBeats;   // 코드가 연주되는 박자 수
//    }

}

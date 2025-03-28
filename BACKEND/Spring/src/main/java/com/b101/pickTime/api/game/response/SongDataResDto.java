package com.b101.pickTime.api.game.response;

import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SongDataResDto {

    private Integer songId;
    private String title;
    private int bpm;
    private String songUri;
    private String songThumbnailUri;
    private List<String> chords;
    private int star;

}

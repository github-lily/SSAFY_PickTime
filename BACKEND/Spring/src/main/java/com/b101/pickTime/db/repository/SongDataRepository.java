package com.b101.pickTime.db.repository;

import com.b101.pickTime.api.game.response.SongGameResDto;
import com.b101.pickTime.db.document.SongData;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface SongDataRepository extends MongoRepository<SongData, Integer> {

    @Query(
            value = "{ '_id': ?0 }",
            fields = "{ 'title': 1, 'bpm': 1, 'artist': 1, 'durationSec' : 1,'timeSignature': 1, 'chordProgression': 1, 'songUri': 1 }"
    )
    SongGameResDto findSongGameResDtoBySongDataId(Integer songId);

}

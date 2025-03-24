package com.b101.pickTime.db.repository;

import com.b101.pickTime.api.chord.response.ChordDataResDto;
import com.b101.pickTime.db.document.ChordData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;


public interface ChordDataRepository extends MongoRepository<ChordData, Integer> {

    @Query(
            value = "{ '_id': ?0 }",
            fields = "{'chordName': 1, 'chordImageUri': 1,'chordSoundUri': 1,'chordFingering': 1}"
    )
    ChordDataResDto findChordDataResDtoById(int chordId);
}

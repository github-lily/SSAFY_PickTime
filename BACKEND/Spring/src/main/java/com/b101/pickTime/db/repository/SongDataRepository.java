package com.b101.pickTime.db.repository;

import com.b101.pickTime.db.document.SongData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SongDataRepository extends MongoRepository<SongData, Integer> {
}

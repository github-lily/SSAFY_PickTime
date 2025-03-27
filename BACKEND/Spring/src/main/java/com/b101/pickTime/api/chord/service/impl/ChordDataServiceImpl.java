package com.b101.pickTime.api.chord.service.impl;

import com.b101.pickTime.api.chord.response.ChordDataResDto;
import com.b101.pickTime.api.chord.service.ChordDataService;
import com.b101.pickTime.db.repository.ChordDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChordDataServiceImpl implements ChordDataService {

    private final ChordDataRepository chordDataRepository;

    @Override
    public ChordDataResDto getChord(int chordId) {
        return chordDataRepository.findChordDataResDtoById(chordId);
    }
}

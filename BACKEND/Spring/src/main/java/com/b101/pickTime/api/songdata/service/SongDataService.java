package com.b101.pickTime.api.songdata.service;

import com.b101.pickTime.api.songdata.response.SongDataResDto;

import java.util.List;

public interface SongDataService {

    List<SongDataResDto> findAllSongs();
}

package com.b101.pickTime.api.game.service;

import com.b101.pickTime.api.game.response.SongDataResDto;
import com.b101.pickTime.api.game.response.SongGameResDto;

import java.util.List;

public interface SongDataService {

    List<SongDataResDto> getAllSongs();
    SongGameResDto getSong(Integer songId);
}

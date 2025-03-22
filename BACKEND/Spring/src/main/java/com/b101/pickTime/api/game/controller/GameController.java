package com.b101.pickTime.api.game.controller;

import com.b101.pickTime.api.songdata.response.SongDataResDto;
import com.b101.pickTime.api.songdata.service.SongDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final SongDataService songDataService;

    @GetMapping
    public ResponseEntity<List<SongDataResDto>> getAllSongs(){
        List<SongDataResDto> songs = songDataService.findAllSongs();

        return ResponseEntity.ok(songs);
    }

}

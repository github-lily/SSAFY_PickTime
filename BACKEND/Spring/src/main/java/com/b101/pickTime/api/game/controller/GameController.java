package com.b101.pickTime.api.game.controller;

import com.b101.pickTime.api.game.response.SongDataResDto;
import com.b101.pickTime.api.game.response.SongGameResDto;
import com.b101.pickTime.api.game.service.SongDataService;
import com.b101.pickTime.common.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final SongDataService songDataService;

    @GetMapping
    public ResponseEntity<List<SongDataResDto>> getAllSongs(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        List<SongDataResDto> songs = songDataService.getAllSongs(customUserDetails.getUserId());

        return ResponseEntity.ok(songs);
    }

    @GetMapping("/{songId}")
    public ResponseEntity<SongGameResDto> getSong(@PathVariable("songId") Integer songId){
        SongGameResDto song = songDataService.getSong(songId);

        return ResponseEntity.ok(song);
    }

}

package com.b101.pickTime.api.completedSong.controller;

import com.b101.pickTime.api.completedSong.request.CompleteSongReqDto;
import com.b101.pickTime.api.completedSong.service.CompletedSongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/completed-song")
@RequiredArgsConstructor
public class CompletedSongController {

    private final CompletedSongService completedGameService;

    @PostMapping("/{songId}")
    public ResponseEntity<String> completeStep(@PathVariable("songId") Integer songId,
                                               @RequestBody CompleteSongReqDto completeSongReqDto){

        completedGameService.completeSong(completeSongReqDto.getUserId(), songId, completeSongReqDto.getScore());

        return ResponseEntity.ok("게임 완료!");

    }

}

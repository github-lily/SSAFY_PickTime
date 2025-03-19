package com.b101.pickTime.api.completedsong.controller;

import com.b101.pickTime.api.completedsong.request.CompletedSongReqDto;
import com.b101.pickTime.api.completedsong.service.CompletedSongService;
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
                                               @RequestBody CompletedSongReqDto completeSongReqDto){

        completedGameService.completeSong(completeSongReqDto.getUserId(), songId, completeSongReqDto.getScore());

        return ResponseEntity.ok("게임 완료!");

    }

}

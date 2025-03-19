package com.b101.pickTime.api.completedSong.service;

public interface CompletedSongService {
    public void completeSong(Integer userId, Integer songId, Integer score);
}

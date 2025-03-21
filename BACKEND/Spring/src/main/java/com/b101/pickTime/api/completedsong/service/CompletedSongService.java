package com.b101.pickTime.api.completedsong.service;

import com.b101.pickTime.api.completedactivities.response.CompletedActivitiesResDto;

import java.util.List;

public interface CompletedSongService {
    public void completeSong(Integer userId, Integer songId, Integer score);
    public List<CompletedActivitiesResDto> getPickDaysOfSong(Integer userId);
}

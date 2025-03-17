package com.b101.pickTime.api.stage.service.impl;

import com.b101.pickTime.api.stage.response.StageResDto;
import com.b101.pickTime.api.stage.service.StageService;
import com.b101.pickTime.db.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StageServiceImpl implements StageService {

    private final StageRepository stageRepository;

    @Override
    public List<StageResDto> getStages() {
        return stageRepository.findAllStage();
    }
}

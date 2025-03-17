package com.b101.pickTime.api.completedstep.service.impl;

import com.b101.pickTime.api.completedstep.service.CompletedStepService;
import com.b101.pickTime.db.repository.CompletedStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompletedStepServceImpl implements CompletedStepService {

    private final CompletedStepRepository completedStepRepository;



}

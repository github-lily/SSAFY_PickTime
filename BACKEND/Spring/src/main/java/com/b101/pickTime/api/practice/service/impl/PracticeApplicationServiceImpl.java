package com.b101.pickTime.api.practice.service.impl;

import com.b101.pickTime.api.chord.response.ChordDataResDto;
import com.b101.pickTime.api.chord.service.ChordDataService;
import com.b101.pickTime.api.completedstep.service.CompletedStepService;
import com.b101.pickTime.api.game.service.SongDataService;
import com.b101.pickTime.api.practice.response.CurriculumResDto;
import com.b101.pickTime.api.practice.response.PracticeResDto;
import com.b101.pickTime.api.practice.service.PracticeApplicationService;
import com.b101.pickTime.api.stage.response.StageResDto;
import com.b101.pickTime.api.stage.service.StageService;
import com.b101.pickTime.api.step.response.StepInfoResDto;
import com.b101.pickTime.api.step.response.StepResDto;
import com.b101.pickTime.api.step.servce.StepService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PracticeApplicationServiceImpl implements PracticeApplicationService {

    private final StageService stageService;
    private final StepService stepService;
    private final CompletedStepService completedStepService;
    private final SongDataService songDataService;
    private final ChordDataService chordDataService;

    @Override
    public CurriculumResDto getCurriculum(Integer userId){
        List<StageResDto> stages = stageService.getStages();
        for(StageResDto stage : stages){
            List<StepResDto> steps = stepService.getSteps(stage.getStageId(), userId);
            stage.setSteps(steps);
            boolean isClear = true;
            for(StepResDto step : steps){
                if(!step.getIsClear()){
                    isClear = false;
                    break;
                }
            }

            stage.setIsClear(isClear);
        }

        double clearRate = completedStepService.getProgress(userId);

        return new CurriculumResDto(stages, clearRate);
    }

    public PracticeResDto getStep(Integer stepId){

        StepInfoResDto stepInfo = stepService.getStepInfo(stepId);
        PracticeResDto step = new PracticeResDto();

        step.setStepType(stepInfo.getStepType());

        switch (step.getStepType()){
            case 1:
                ChordDataResDto chord = chordDataService.getChord(stepInfo.getChordId());
                List<ChordDataResDto> chordForPractice = List.of(chord);
                step.setChords(chordForPractice);
                break;

            case 2:
                List<Integer> chords = stepService.getChordsFromStage(stepInfo.getStageId());
                List<ChordDataResDto> chordsForPractice = new ArrayList<>();

                for(int chordId : chords){
                    chordsForPractice.add(chordDataService.getChord(chordId));
                }

                step.setChords(chordsForPractice);
                break;

            case 3:
                step.setSong(songDataService.getSong(stepInfo.getSongId()));
                break;
        }

        return step;
    }

}

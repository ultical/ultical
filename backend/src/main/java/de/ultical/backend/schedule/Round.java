package de.ultical.backend.schedule;

import java.util.List;

import lombok.Getter;

public class Round {

    public Round(Phase phase) {
        this.phase = phase;
    }

    @Getter
    protected List<Game> games;

    protected int phaseIdx;

    @Getter
    protected Phase phase;

    public int getTimingIndex() {
        return this.phase.timingIndex * 10000 + this.phaseIdx;
    }

}

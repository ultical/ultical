package de.ultical.backend.schedule;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Round {

    private String title;
    private List<Game> games;
    private int timingIndex;
    private Phase phase;

    public Round(Phase phase) {
        this.phase = phase;
        this.games = new ArrayList<Game>();
    }

    public int getPhaseTimingIndex() {
        return this.phase.getTimingIndex() * 10000 + this.timingIndex;
    }

    public void addGame(Game game) {
        this.games.add(game);
    }

}

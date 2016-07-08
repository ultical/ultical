package de.ultical.backend.schedule;

import java.util.Map;

import de.ultical.backend.model.Team;

public interface PhaseInterface {

    // public getGames();
    public boolean isFinished();

    public Map<Integer, Team> getStandings();
}

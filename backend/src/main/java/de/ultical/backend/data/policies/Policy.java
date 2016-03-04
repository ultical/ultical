package de.ultical.backend.data.policies;

import java.util.Map;

import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.TournamentEdition;

public interface Policy {

    static public final int OK = 0;
    static public final int ALREADY_IN_DIFFERENT_ROSTER = 1;

    public int addPlayerToRoster(DfvPlayer player, Roster roster);

    public int registerRosterToEdition(Roster roster, TournamentEdition tournamentEdition);

    public Map<String, String> getErrorParameters();
}

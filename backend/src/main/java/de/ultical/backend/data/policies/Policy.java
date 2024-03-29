package de.ultical.backend.data.policies;

import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Context;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.TournamentEdition;

import java.util.Map;

public interface Policy {

    static public final int OK = 0;
    static public final int ALREADY_IN_DIFFERENT_ROSTER = 1;

    enum Eligibility {
        ELIGIBLE, NOT_PAID, NO_DSE, NOT_ACTIVE, IDLE;
    }

    public int addPlayerToRoster(DfvPlayer player, Roster roster);

    public int registerRosterToEdition(Roster roster, TournamentEdition tournamentEdition);

    public Eligibility getPlayerEligibility(DfvMvPlayer player);

    public Map<String, String> getErrorParameters();

    public static Policy getPolicy(Context context, DataStore dataStore) {
        if (context == null) {
            return new DefaultPolicy(dataStore);
        }
        return getPolicy(context.getAcronym().toUpperCase(), dataStore);
    }

    public static Policy getPolicy(String contextName, DataStore dataStore) {
        switch (contextName) {
            case "DFV":
                return new DfvPolicy(dataStore);
            default:
                return new DefaultPolicy(dataStore);
        }
    }
}

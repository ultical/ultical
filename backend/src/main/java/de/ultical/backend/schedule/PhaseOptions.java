package de.ultical.backend.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhaseOptions {

    // POOL PHASE OPTIONS
    public static final String POOL_HIGHEST_SEEDS_PLAY_FIRST = "highest_seeds_play_first"; // false
    public static final String POOL_SHOW_GAMES_WITH_BYE = "show_games_with_bye"; // false
    public static final String POOL_SHOW_GAMES_WITH_NO_SHOW = "show_games_with_no_show"; // false
    // -- TIE BREAKERS
    // the format to put in the options map is
    // "tie_breaker_<rank>" --> "<string of tie breaker>"
    // example: "tie_breaker_2" --> "point_difference_tied_teams"
    public static final String POOL_TIE_BREAKER_RANKING_TEMPLATE = "tie_breaker_";
    public static final String POOL_TIE_BREAKER_NUMBER_OF_WINS_POOL = "tie_breaker_number_of_wins_pool"; // 0
    public static final String POOL_TIE_BREAKER_NUMBER_OF_WINS_TIED_TEAMS = "tie_breaker_number_of_wins_tied_teams"; // 1
    public static final String POOL_TIE_BREAKER_POINT_DIFFERENCE_TIED_TEAMS = "tie_breaker_point_difference_tied_teams"; // 2
    public static final String POOL_TIE_BREAKER_POINT_DIFFERENCE_POOL = "tie_breaker_point_difference_pool"; // 3
    public static final String POOL_TIE_BREAKER_SCORED_POINTS_TIED_TEAMS = "tie_breaker_scored_points_tied_teams"; // 4
    public static final String POOL_TIE_BREAKER_SCORED_POINTS_POOL = "tie_breaker_scored_points_pool"; // 5
    public static final String POOL_TIE_BREAKER_BY_LOT = "tie_breaker_by_lot"; // 6

    // BRACKET PHASE OPTIONS
    public static final String PLAY_LOOSER_BRACKET = "play_looser_bracket"; // true

    Map<String, String> entries;

    public PhaseOptions() {
        this.entries = new HashMap<String, String>();
    }

    public boolean containsKey(String key) {
        return this.entries.containsKey(key);
    }

    public String get(String key) {
        return this.entries.get(key);
    }

    public Map<Integer, String> getTieBreakOrder() {
        Map<Integer, String> tieBreakOrder = new HashMap<Integer, String>();

        // check if we have to re-assemble the tie breaker
        boolean doTieBreaker = false;
        for (int i = 0; i < 20; i++) {
            if (this.entries.containsKey(POOL_TIE_BREAKER_RANKING_TEMPLATE + i)) {
                doTieBreaker = true;
            }
        }

        if (doTieBreaker) {
            List<String> tieBreakerList = new ArrayList<String>();

            // create ordered list
            for (int i = 0; i < 20; i++) {
                if (this.entries.containsKey(POOL_TIE_BREAKER_RANKING_TEMPLATE + i)) {
                    tieBreakerList.add(this.entries.get(POOL_TIE_BREAKER_RANKING_TEMPLATE + i));
                }
            }

            // create map
            int i = 0;
            for (String tieBreakString : tieBreakerList) {
                tieBreakOrder.put(i, tieBreakString);
                i++;
            }
        } else {
            // those are the default values
            tieBreakOrder.put(0, POOL_TIE_BREAKER_NUMBER_OF_WINS_POOL);
            tieBreakOrder.put(1, POOL_TIE_BREAKER_NUMBER_OF_WINS_TIED_TEAMS);
            tieBreakOrder.put(2, POOL_TIE_BREAKER_POINT_DIFFERENCE_TIED_TEAMS);
            tieBreakOrder.put(3, POOL_TIE_BREAKER_POINT_DIFFERENCE_POOL);
            tieBreakOrder.put(4, POOL_TIE_BREAKER_SCORED_POINTS_TIED_TEAMS);
            tieBreakOrder.put(5, POOL_TIE_BREAKER_SCORED_POINTS_POOL);
            tieBreakOrder.put(6, POOL_TIE_BREAKER_BY_LOT);
        }

        return tieBreakOrder;
    }

    public boolean isTrue(String optionKey) {
        String optionValue = this.entries.get(optionKey.toLowerCase());
        if (optionValue == null) {
            return false;
        }
        if (optionValue.equalsIgnoreCase("true") || optionValue.equals("1")) {
            return true;
        }
        return false;
    }

}

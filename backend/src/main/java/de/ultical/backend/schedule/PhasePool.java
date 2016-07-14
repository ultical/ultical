package de.ultical.backend.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;

public class PhasePool extends Phase {

    Map<Integer, String> tieBreakOrder;

    @Data
    public class PoolStats {
        TeamRepresentation teamRep;
        // 2 = win, 1 = draw, 0 = loss
        private int winnerPoints = 0;
        private int scoredPoints = 0;
        private int receivedPoints = 0;
        private int gamesPlayed = 0;

        public PoolStats(TeamRepresentation teamRep) {
            this.teamRep = teamRep;
        }
    }

    public class PoolStatsComparator implements Comparator<PoolStats> {
        public static final int SORT_BY_WINNER_POINTS = 0;
        public static final int SORT_BY_POINT_DIFFERENCE = 1;
        public static final int SORT_BY_SCORED_POINTS = 2;

        private int sortBy;

        public PoolStatsComparator(int sortByString) {
            this.sortBy = sortByString;
        }

        @Override
        public int compare(PoolStats stats1, PoolStats stats2) {
            int comparationResult = 0;
            switch (this.sortBy) {
            case SORT_BY_WINNER_POINTS:
                comparationResult = Integer.compare(stats1.getWinnerPoints(), stats2.getWinnerPoints());
                break;
            case SORT_BY_SCORED_POINTS:
                comparationResult = Integer.compare(stats1.getScoredPoints(), stats2.getScoredPoints());
                break;
            case SORT_BY_POINT_DIFFERENCE:
                comparationResult = Integer.compare(stats1.getScoredPoints() - stats1.getReceivedPoints(),
                        stats2.getScoredPoints() - stats2.getReceivedPoints());
                break;
            }
            // inverse the number for descending order
            return comparationResult * -1;
        }
    }

    public PhasePool(String name, int numTeams) {
        super(name, numTeams);
        this.tieBreakOrder = this.getOptions().getTieBreakOrder();
    }

    @Override
    public void finalizeCreation() {

        // ask in the previous phases for (current) standings
        this.createTeamInputMapping();

        // create list of seeds with number 1 seed missing
        List<Integer> seedingList = new ArrayList<Integer>();
        for (int i = 2; i <= this.numTeams; i++) {
            seedingList.add(i);
        }

        Map<Integer, TeamRepresentation> workingInputMapping = new HashMap<Integer, TeamRepresentation>(
                this.getInputMapping());

        // if odd number of teams add a dummy
        if (workingInputMapping.size() % 2 != 0) {
            seedingList.add(-1);
            workingInputMapping.put(-1, new TeamRepresentation(true));
            workingInputMapping.get(-1).setSeed(workingInputMapping.size());
        }

        for (int i = 0; i < seedingList.size(); i++) {

            Round round = new Round(this);
            round.setTimingIndex(i);

            int teamIdx = round.getTimingIndex() % seedingList.size();

            Game game;

            game = new Game();
            game.setTeam1(workingInputMapping.get(seedingList.get(teamIdx)));
            game.setTeam2(workingInputMapping.get(1));
            if ((!game.hasBye() || this.getOptions().isTrue(PhaseOptions.POOL_SHOW_GAMES_WITH_BYE))
                    && (!game.hasNoShow() || this.getOptions().isTrue(PhaseOptions.POOL_SHOW_GAMES_WITH_NO_SHOW))) {
                round.addGame(game);
            }

            for (int idx = 1; idx < (seedingList.size() + 1) / 2; idx++) {
                int idxTeam1 = (round.getTimingIndex() + idx) % seedingList.size();
                int idxTeam2 = (round.getTimingIndex() + seedingList.size() - idx) % seedingList.size();

                game = new Game();
                game.setTeam1(workingInputMapping.get(seedingList.get(idxTeam1)));
                game.setTeam2(workingInputMapping.get(seedingList.get(idxTeam2)));
                if ((!game.hasBye() || this.getOptions().isTrue(PhaseOptions.POOL_SHOW_GAMES_WITH_BYE))
                        && (!game.hasNoShow() || this.getOptions().isTrue(PhaseOptions.POOL_SHOW_GAMES_WITH_NO_SHOW))) {
                    round.addGame(game);
                }
            }

            this.getRounds().add(round);
        }

        // the highest seeds should play each other last if not specified
        // otherwise in the options
        if (!this.getOptions().isTrue(PhaseOptions.POOL_HIGHEST_SEEDS_PLAY_FIRST)) {
            int i = 1;
            for (Round round : this.getRounds()) {
                round.setTimingIndex(this.getRounds().size() - i);
                i++;
            }
            Collections.reverse(this.getRounds());
        }
    }

    @Override
    public int getNumRounds() {
        return this.numTeams - (int) Math.floor(this.numTeams / 2);
    }

    @Override
    protected Map<Integer, TeamRepresentation> updateStandings() {

        boolean allGamesPlayed = true;

        for (Game game : this.getGames()) {
            if (!game.isOver()) {
                allGamesPlayed = false;
            }
        }

        List<TeamRepresentation> standingsList = this
                .breakTie(new ArrayList<TeamRepresentation>(this.getInputMapping().values()));

        Map<Integer, TeamRepresentation> standingsMap = new HashMap<Integer, TeamRepresentation>();
        int rank = 0;
        for (TeamRepresentation teamRep : standingsList) {
            standingsMap.put(rank, teamRep);
            rank++;
        }

        // if all games are played, apply mapping to output mapping to provide
        // for next phase
        if (allGamesPlayed) {
            this.setComplete(true);
            this.setOutputMapping(standingsMap);
        }

        return standingsMap;
    }

    private List<TeamRepresentation> breakTie(List<TeamRepresentation> tiedTeams) {
        return this.breakTie(tiedTeams, 0);
    }

    private List<TeamRepresentation> breakTie(List<TeamRepresentation> tiedTeams, int tieBreakerLevel) {

        if (tieBreakerLevel >= this.tieBreakOrder.size()) {
            return new ArrayList<TeamRepresentation>();
        }

        if (tiedTeams.size() < 2) {
            return tiedTeams;
        }

        List<TeamRepresentation> rankedTeams = new ArrayList<TeamRepresentation>();

        switch (this.tieBreakOrder.get(tieBreakerLevel)) {
        case PhaseOptions.POOL_TIE_BREAKER_NUMBER_OF_WINS_POOL:
            return this.tieBreakHelper(tiedTeams, PoolStatsComparator.SORT_BY_WINNER_POINTS, true, tieBreakerLevel);
        case PhaseOptions.POOL_TIE_BREAKER_NUMBER_OF_WINS_TIED_TEAMS:
            return this.tieBreakHelper(tiedTeams, PoolStatsComparator.SORT_BY_WINNER_POINTS, false, tieBreakerLevel);
        case PhaseOptions.POOL_TIE_BREAKER_POINT_DIFFERENCE_TIED_TEAMS:
            return this.tieBreakHelper(tiedTeams, PoolStatsComparator.SORT_BY_POINT_DIFFERENCE, false, tieBreakerLevel);
        case PhaseOptions.POOL_TIE_BREAKER_POINT_DIFFERENCE_POOL:
            return this.tieBreakHelper(tiedTeams, PoolStatsComparator.SORT_BY_POINT_DIFFERENCE, true, tieBreakerLevel);
        case PhaseOptions.POOL_TIE_BREAKER_SCORED_POINTS_TIED_TEAMS:
            return this.tieBreakHelper(tiedTeams, PoolStatsComparator.SORT_BY_SCORED_POINTS, false, tieBreakerLevel);
        case PhaseOptions.POOL_TIE_BREAKER_SCORED_POINTS_POOL:
            return this.tieBreakHelper(tiedTeams, PoolStatsComparator.SORT_BY_SCORED_POINTS, true, tieBreakerLevel);
        case PhaseOptions.POOL_TIE_BREAKER_BY_LOT:
            // TODO: do the drawing of a random winner
            break;
        }

        return rankedTeams;
    }

    private List<TeamRepresentation> tieBreakHelper(List<TeamRepresentation> tiedTeams, int sortBy,
            boolean considerWholePool, int tieBreakerLevel) {

        // create stats and order list according to parameters
        List<PoolStats> teamStats = this.createTeamsStats(tiedTeams, considerWholePool);
        Collections.sort(teamStats, new PoolStatsComparator(sortBy));

        List<TeamRepresentation> orderedList = new ArrayList<TeamRepresentation>();

        Set<TeamRepresentation> tempTiedSet = null;

        // compare the order and solve ties by recursive calls
        for (int i = 0; i < teamStats.size(); i++) {
            int compareValuePrevious = -1, compareValueThis = -1;

            // compare to last element
            switch (sortBy) {
            case PoolStatsComparator.SORT_BY_WINNER_POINTS:
                if (i > 0) {
                    compareValuePrevious = teamStats.get(i - 1).getWinnerPoints();
                    compareValueThis = teamStats.get(i).getWinnerPoints();
                }
                break;
            case PoolStatsComparator.SORT_BY_SCORED_POINTS:
                if (i > 0) {
                    compareValuePrevious = teamStats.get(i - 1).getScoredPoints();
                    compareValueThis = teamStats.get(i).getScoredPoints();
                }
                break;
            case PoolStatsComparator.SORT_BY_POINT_DIFFERENCE:
                if (i > 0) {
                    compareValuePrevious = teamStats.get(i - 1).getScoredPoints()
                            - teamStats.get(i - 1).getReceivedPoints();
                    compareValueThis = teamStats.get(i).getScoredPoints() - teamStats.get(i - 1).getReceivedPoints();
                }
                break;
            }

            if (i == 0 || compareValuePrevious != compareValueThis) {
                // there is no tie with this index

                // but if there has been a tie before we need to resolve this
                // first
                if (tempTiedSet != null) {
                    orderedList.addAll(this.breakTie(new ArrayList<TeamRepresentation>(tempTiedSet), 0));
                } else if (i > 0) {
                    orderedList.add(teamStats.get(i - 1).getTeamRep());
                }

                tempTiedSet = null;
            } else {
                // this element and the previous one have a tie
                if (tempTiedSet == null) {
                    tempTiedSet = new HashSet<TeamRepresentation>();
                }
                // add the previous and this item
                tempTiedSet.add(teamStats.get(i - 1).getTeamRep());
                tempTiedSet.add(teamStats.get(i).getTeamRep());
            }
        }

        if (tempTiedSet != null) {
            // check if it contains all the elements of "tiedTeams" ...

            if (tempTiedSet.size() == tiedTeams.size()) {
                // ...yes --> take tie breaking to the next level
                return this.breakTie(tiedTeams, tieBreakerLevel + 1);
            } else {
                // ...no --> this is just a subgroup, restart tie breaking for
                // this group at level 0
                orderedList.addAll(this.breakTie(new ArrayList<TeamRepresentation>(tempTiedSet), 0));
            }
        } else {
            orderedList.add(teamStats.get(teamStats.size() - 1).getTeamRep());
        }

        return orderedList;
    }

    /**
     * Create stats only for the team in the given list
     *
     * @param tiedTeams
     *            List of teams that are to be considered
     * @return The input list with stats information filled in
     */
    private List<PoolStats> createTeamsStats(List<TeamRepresentation> teams, boolean considerWholePool) {

        List<PoolStats> teamStats = new ArrayList<PoolStats>();

        for (TeamRepresentation team : teams) {
            teamStats.add(new PoolStats(team));
        }

        for (Game game : this.getGames()) {
            if (game.getFinalScore1() < 0 || game.getFinalScore2() < 0) {
                continue;
            }
            PoolStats team1Stat = null, team2Stat = null;
            // get teams playing this game
            for (PoolStats poolStats : teamStats) {
                if (game.getTeam1().equals(poolStats.getTeamRep())) {
                    team1Stat = poolStats;
                }
                if (game.getTeam2().equals(poolStats.getTeamRep())) {
                    team2Stat = poolStats;
                }
            }

            // check if we consider whole pool or only games within the tied
            // teams
            if (considerWholePool || (team1Stat != null && team2Stat != null)) {
                if (team1Stat != null) {
                    team1Stat.setGamesPlayed(team1Stat.getGamesPlayed() + 1);
                    team1Stat.setWinnerPoints(team1Stat.getWinnerPoints() + (game.getWinningTendency() * -1) + 1);
                    team1Stat.setScoredPoints(team1Stat.getScoredPoints() + game.getFinalScore1());
                    team1Stat.setReceivedPoints(team1Stat.getReceivedPoints() + game.getFinalScore2());
                }
                if (team2Stat != null) {
                    team2Stat.setGamesPlayed(team2Stat.getGamesPlayed() + 1);
                    team2Stat.setWinnerPoints(team2Stat.getWinnerPoints() + game.getWinningTendency() + 1);
                    team2Stat.setScoredPoints(team2Stat.getScoredPoints() + game.getFinalScore2());
                    team2Stat.setReceivedPoints(team2Stat.getReceivedPoints() + game.getFinalScore1());
                }
            }
        }
        return teamStats;
    }
}

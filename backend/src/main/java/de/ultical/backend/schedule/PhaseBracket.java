package de.ultical.backend.schedule;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

public class PhaseBracket extends Phase {

    public static final String[] ROUND_TITLES = { "FINAL", "SF", "QF", "R16", "R32", "R64", "R128", "R256" };

    public static final String WINNER = "Winner";
    public static final String LOOSER = "Looser";

    public PhaseBracket(String name, int numTeams) {
        super(name, numTeams);
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public class BracketGame extends Game {
        private BracketGame nextGameWinner;
        private BracketGame nextGameLooser;
    }

    @Override
    public void finalizeCreation() {

        // ask in the previous phase for (current) standings
        this.createTeamInputMapping();

        Map<Integer, TeamRepresentation> teamList = new HashMap<Integer, TeamRepresentation>(this.getInputMapping());

        // if there are less teams than 2^x fill the list up with byes
        while (Math.ceil(Math.log(teamList.size()) / Math.log(2)) > (Math.log(teamList.size()) / Math.log(2))) {
            teamList.put(teamList.size() + 1, new TeamRepresentation(true));
            teamList.get(teamList.size()).setSeed(teamList.size());
        }

        for (int roundNumber = 0; roundNumber < this.getNumRounds(); roundNumber++) {
            Round round = new Round(this);
            round.setTimingIndex(roundNumber);
            round.setTitle(this.getName() + " " + ROUND_TITLES[this.getNumRounds() - roundNumber - 1]);
            this.getRounds().add(round);
        }

        int currentSeed = 1;

        for (int gameNumber = 0; gameNumber < Math.pow(2, this.getNumRounds() - 1); gameNumber++) {

            Game game = new Game();
            game.setIdentifier(ROUND_TITLES[this.getNumRounds() - 1] + "-1-" + (1 + gameNumber));
            if (gameNumber > 0) {
                /**
                 * next game is determined by algorithm: the next game is always
                 * the opponent of the last game's higher seed if it was played
                 * with half the teams
                 */
                currentSeed = this.getOpponent(teamList.size() / 2, currentSeed);
            }
            game.setTeam1(teamList.get(currentSeed));
            game.setTeam2(teamList.get(this.getOpponent(teamList.size(), currentSeed)));

            this.getRounds().get(0).addGame(game);
        }

        this.createGames(1, 1, false, teamList.size() / 2);
        this.createGames(1, teamList.size() / 2 + 1, true, teamList.size() / 2);
    }

    private void createGames(int roundNumber, int highestRank, boolean looserRound, int numberOfTeams) {

        Game game;

        for (int gameNumber = 0; gameNumber < numberOfTeams / 2; gameNumber++) {
            game = new Game();

            if (!looserRound) {
                // take the winners of the previous round
                game.setIdentifier(ROUND_TITLES[this.getNumRounds() - roundNumber - 1] + "-" + highestRank + "-"
                        + (1 + gameNumber));
                Game game1 = this.getRounds().get(roundNumber - 1).getGames()
                        .get((highestRank - 1) / 2 + gameNumber * 2);
                Game game2 = this.getRounds().get(roundNumber - 1).getGames()
                        .get((highestRank - 1) / 2 + (gameNumber * 2) + 1);

                game.setTeam1(new TeamRepresentation(WINNER + " " + game1.getIdentifier()));
                game.setTeam2(new TeamRepresentation(WINNER + " " + game2.getIdentifier()));
                if (game1.hasBye() || game1.hasNoShow()) {
                    game.setTeam1(game1.getWinner());
                } else if (game2.hasBye() || game2.hasNoShow()) {
                    game.setTeam2(game2.getWinner());
                }
            } else {
                // Looser part of bracket
                game.setIdentifier(ROUND_TITLES[this.getNumRounds() - roundNumber - 1] + "-" + highestRank + "-"
                        + (1 + gameNumber));

                game.setTeam1(new TeamRepresentation(LOOSER + " " + this.getRounds().get(roundNumber - 1).getGames()
                        .get(((highestRank - 1 - numberOfTeams) / 2) + gameNumber * 2).getIdentifier()));
                game.setTeam2(new TeamRepresentation(LOOSER + " " + this.getRounds().get(roundNumber - 1).getGames()
                        .get(((highestRank - 1 - numberOfTeams) / 2) + gameNumber * 2 + 1).getIdentifier()));
            }

            this.getRounds().get(roundNumber).addGame(game);
        }

        // stop if we are in the last round
        if (this.getNumRounds() - roundNumber > 1) {
            // winner streak
            this.createGames(roundNumber + 1, highestRank, false, numberOfTeams / 2);
            // looser streak
            this.createGames(roundNumber + 1, highestRank + ((int) Math.pow(2, this.getNumRounds() - roundNumber - 1)),
                    true, numberOfTeams / 2);
        }
    }

    private int getOpponent(int numTeams, int seed) {

        if (numTeams == seed) {
            return numTeams / 2;
        }
        if (numTeams / 2 < seed) {
            return this.getOpponent(numTeams / 2, numTeams - seed + 1);
        }

        return numTeams - (seed - 1);
    }

    @Override
    protected Map<Integer, TeamRepresentation> updateStandings() {

        // ask in the previous phases for (current) standings
        this.createTeamInputMapping();

        boolean allGamesPlayed = true;

        for (Game game : this.getGames()) {
            if (!game.isOver()) {
                allGamesPlayed = false;
            }
        }

        Map<Integer, TeamRepresentation> standingsMap = new HashMap<Integer, TeamRepresentation>();

        // go through all games and check for teams not yet filled in (because
        // previous round has not been ready back then)
        for (int roundNumber = 0; roundNumber < this.getNumRounds(); roundNumber++) {

            for (Game game : this.getRounds().get(roundNumber).getGames()) {

                if (roundNumber == 0) {
                    // first round - take from inputMapping / seeding
                    if (!game.getTeam1().isBye()) {
                        game.getTeam1().setTeam(this.getInputMapping().get(game.getTeam1().getSeed()).getTeam());
                    }
                    if (!game.getTeam2().isBye()) {
                        game.getTeam2().setTeam(this.getInputMapping().get(game.getTeam2().getSeed()).getTeam());
                    }
                } else {
                    // not first round - take from previous games
                    if (game.getTeam1().getTeam() == null) {
                        String[] prevGame1Parts = game.getTeam1().getTitle().split(" ");
                        Game prevGame1 = this.findGame(roundNumber - 1, prevGame1Parts[1]);
                        if (prevGame1.isOver() && prevGame1.getWinningTendency() != 0) {
                            if (prevGame1Parts[0].equals(PhaseBracket.WINNER)) {
                                game.getTeam1().setTeam(prevGame1.getWinner().getTeam());
                            } else {
                                if (prevGame1.getLooser().isBye()) {
                                    game.getTeam1().setBye(true);
                                } else {
                                    game.getTeam1().setTeam(prevGame1.getLooser().getTeam());
                                }
                            }
                        }
                    }
                    if (game.getTeam2().getTeam() == null) {
                        String[] prevGame2Parts = game.getTeam2().getTitle().split(" ");
                        Game prevGame2 = this.findGame(roundNumber - 1, prevGame2Parts[1]);
                        if (prevGame2.isOver() && prevGame2.getWinningTendency() != 0) {
                            if (prevGame2Parts[0].equals(PhaseBracket.WINNER)) {
                                game.getTeam2().setTeam(prevGame2.getWinner().getTeam());
                            } else {
                                if (prevGame2.getLooser().isBye()) {
                                    game.getTeam2().setBye(true);
                                } else {
                                    game.getTeam2().setTeam(prevGame2.getLooser().getTeam());
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Game game : this.getRounds().get(this.getNumRounds() - 1).getGames()) {
            // create ranking
            String[] gameNameParts = game.getIdentifier().split("-");
            standingsMap.put(Integer.parseInt(gameNameParts[1]), game.getWinner());
            standingsMap.put(Integer.parseInt(gameNameParts[1]) + 1, game.getLooser());
        }

        // if all games are played, apply mapping to output mapping to provide
        // for next phase
        if (allGamesPlayed) {
            this.setComplete(true);
            this.setOutputMapping(standingsMap);
        }

        return standingsMap;
    }

    private Game findGame(int roundNumber, String identifier) {
        for (Game game : this.getRounds().get(roundNumber).getGames()) {
            if (game.getIdentifier().equalsIgnoreCase(identifier)) {
                return game;
            }
        }
        return null;
    }

    @Override
    public int getNumRounds() {
        return (int) Math.ceil(Math.log(this.numTeams) / Math.log(2));
    }

}

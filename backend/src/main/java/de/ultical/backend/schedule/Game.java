package de.ultical.backend.schedule;

import lombok.Data;

@Data
public class Game {
    private TeamRepresentation team1;
    private TeamRepresentation team2;
    private int finalScore1 = -1;
    private int finalScore2 = -1;
    private String identifier = "";
    private Phase round;
    private boolean over;

    private int timingIndex = 0;

    public boolean didStart() {
        return this.finalScore1 >= 0 || this.finalScore2 >= 0;
    }

    @Override
    public String toString() {
        String output = this.team1.getName() + " - " + this.finalScore1 + " : " + this.finalScore2 + " - "
                + this.team2.getName();

        return output;
    }

    public boolean hasBye() {
        return (this.team1 != null && this.team2 != null) && (this.team1.isBye() || this.team2.isBye());
    }

    public boolean hasNoShow() {
        return (this.team1 != null && this.team2 != null) && (this.team1.isNoShow() || this.team2.isNoShow());
    }

    public int getWinningTendency() {
        return this.finalScore1 > this.finalScore2 ? -1 : (this.finalScore1 == this.finalScore2 ? 0 : 1);
    }

    public boolean isOver() {
        return this.over || this.hasBye();
    }
}

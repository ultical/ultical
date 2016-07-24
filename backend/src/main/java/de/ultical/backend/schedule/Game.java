package de.ultical.backend.schedule;

import lombok.Data;

@Data
public class Game {
    private TeamRepresentation home;
    private TeamRepresentation away;
    private int finalScoreHome = -1;
    private int finalScoreAway = -1;
    private String identifier = "";
    private Phase round;
    private boolean over;
    private SpiritSheet spiritSheet;
    private int timingIndex = 0;

    public boolean didStart() {
        return this.finalScoreHome >= 0 || this.finalScoreAway >= 0;
    }

    @Override
    public String toString() {
        String output = this.getIdentifier() + " " + this.home.getName() + " - " + this.finalScoreHome + " : "
                + this.finalScoreAway + " - " + this.away.getName();

        return output;
    }

    public boolean hasBye() {
        return (this.home != null && this.away != null) && (this.home.isBye() || this.away.isBye());
    }

    public boolean hasNoShow() {
        return (this.home != null && this.away != null) && (this.home.isNoShow() || this.away.isNoShow());
    }

    public int getWinningTendency() {
        if (this.hasBye() || this.hasNoShow()) {
            if (this.getHome().isBye() || this.getHome().isNoShow()) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return this.finalScoreHome > this.finalScoreAway ? -1 : (this.finalScoreHome == this.finalScoreAway ? 0 : 1);
        }
    }

    public boolean isOver() {
        return this.over || this.hasBye();
    }

    public TeamRepresentation getWinner() {
        int wt = this.getWinningTendency();
        return wt < 0 ? this.getHome() : wt > 0 ? this.getAway() : null;
    }

    public TeamRepresentation getLooser() {
        int wt = this.getWinningTendency();
        return wt < 0 ? this.getAway() : wt > 0 ? this.getHome() : null;
    }
}

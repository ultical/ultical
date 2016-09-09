package de.ultical.backend.api.transferClasses;

import lombok.Data;

@Data
public class DfvMvPlayer {
    private String dobString;
    private int club;
    private boolean idle;
    private boolean active;
    private int dfvNumber;
    private String gender;
    private boolean dse;
    private String email;

    public void setGeburtsdatum(String dobString) {
        this.dobString = dobString;
    }

    public void setVerein(int club) {
        this.club = club;
    }

    public void setRuht(boolean idle) {
        this.idle = idle;
    }

    public void setAktiv(boolean active) {
        this.active = active;
    }

    public void setDfvnr(int dfvNumber) {
        this.dfvNumber = dfvNumber;
    }

    public void setGeschlecht(String gender) {
        this.gender = gender;
    }

    public boolean hasDse() {
        return this.isDse();
    }
}

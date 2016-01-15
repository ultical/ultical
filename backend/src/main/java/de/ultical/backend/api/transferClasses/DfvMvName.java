package de.ultical.backend.api.transferClasses;

import lombok.Data;

@Data
public class DfvMvName {
    private String lastName;
    private String firstName;
    private boolean dse;
    private int dfvNumber;
    private String club;

    public String getVorname() {
        return this.firstName;
    }

    public void setVorname(String vorname) {
        this.firstName = vorname;
    }

    public String getNachname() {
        return this.lastName;
    }

    public void setNachname(String nachname) {
        this.lastName = nachname;
    }

    public int getDfvnr() {
        return this.dfvNumber;
    }

    public void setDfvnr(int dfvnr) {
        this.dfvNumber = dfvnr;
    }
}

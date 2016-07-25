package de.ultical.backend.api.transferClasses;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import de.ultical.backend.model.Club;
import lombok.Data;

@Data
public class DfvMvName {
    private String lastName;
    private String firstName;
    private boolean dse;
    private int dfvNumber;
    private Club club;
    private boolean active;

    private LocalDateTime lastModified;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ssXXX")
    public void setMtime(Date lastModifiedDate) {
        this.lastModified = LocalDateTime.ofInstant(lastModifiedDate.toInstant(), ZoneId.systemDefault());
    }

    public void setVnr(int clubNumber) {
        this.club = new Club();
        this.club.setId(clubNumber);
    }

    public void setVorname(String vorname) {
        this.setFirstName(vorname);
    }

    public void setNachname(String nachname) {
        this.setLastName(nachname);
    }

    public void setDfvnr(int dfvnr) {
        this.setDfvNumber(dfvnr);
    }
}

package de.ultical.backend.model;

import lombok.Data;

@Data
public class Club {
    private String name;
    private int id;
    private int association;

    public void setVereinsnr(String clubNumber) {
        this.id = Integer.parseInt(clubNumber);
    }

    public void setVerband(int association) {
        this.association = association;
    }

}

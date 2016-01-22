package de.ultical.backend.model;

import lombok.Data;

@Data
public class Club {
    private String name;
    private int id;
    private Association association;

    public void setVereinsnr(String clubNumber) {
        this.id = Integer.parseInt(clubNumber);
    }

    public void setVerband(int associationId) {
        Association association = new Association();
        association.setId(associationId);
        this.association = association;
    }

}

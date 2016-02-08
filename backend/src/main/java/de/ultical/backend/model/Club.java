package de.ultical.backend.model;

import de.ultical.backend.data.mapper.ClubMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Club extends Identifiable {
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

    @Override
    public Class<ClubMapper> getMapper() {
        return ClubMapper.class;
    }
}
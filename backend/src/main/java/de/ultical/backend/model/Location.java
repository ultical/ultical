package de.ultical.backend.model;

import de.ultical.backend.data.mapper.LocationMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Location extends Identifiable {

    private String title;
    private boolean main;
    private double longitude, latitude;
    private String city;
    private String country;
    private String countryCode;
    private String street;
    private String zipCode;
    private String additionalInfo;

    @Override
    public Class<LocationMapper> getMapper() {
        return LocationMapper.class;
    }
}

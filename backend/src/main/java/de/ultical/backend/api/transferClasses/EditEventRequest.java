package de.ultical.backend.api.transferClasses;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ultical.backend.app.LocalDateDeserializer;
import de.ultical.backend.app.LocalDateSerializer;
import de.ultical.backend.data.mapper.EventMapper;
import de.ultical.backend.model.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class EditEventRequest {
  private int matchdayNumber;

  private int tournamentEditionId;
  private String name;

  private String locationStreet;
  private String locationCountry;
  private String locationZip;
  private String locationCity;
  private String locationInfo;

  private List<Integer> divisionRegistrationIds;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate startDate;
  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate endDate;

  private List<User> admins;
  private List<Fee> fees;

  private Contact localOrganizer;

  private String info;

  private List<Resource> resources;
}

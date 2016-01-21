package de.ultical.backend.api.transferClasses;

import java.util.List;

import de.ultical.backend.model.Club;
import de.ultical.backend.model.User;
import lombok.Data;

@Data
public class RegisterResponse {

    public enum RegisterResponseStatus {
        SUCCESS, NOT_FOUND, VALIDATION_ERROR, AMBIGUOUS, AMBIGUOUS_EMAIL, NO_DFV_EMAIL, EMAIL_ALREADY_TAKEN, USER_ALREADY_REGISTERED;
    }

    public RegisterResponseStatus status;
    public String dfvEmail;
    public User user;
    public List<Club> clubs;

    public RegisterResponse(RegisterResponseStatus status) {
        this.status = status;
    }
}

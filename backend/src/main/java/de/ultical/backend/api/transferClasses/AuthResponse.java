package de.ultical.backend.api.transferClasses;

import de.ultical.backend.model.User;
import lombok.Data;

@Data
public class AuthResponse {

    public enum AuthResponseStatus {
        WRONG_CREDENTIALS, EMAIL_NOT_CONFIRMED, DFV_EMAIL_NOT_OPT_IN, SUCCESS
    }

    public AuthResponseStatus status;
    public User user;

    public AuthResponse(AuthResponseStatus status) {
        this.status = status;
    }
}

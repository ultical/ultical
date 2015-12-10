package de.ultical.backend.api.transferClasses;

import de.ultical.backend.model.User;
import lombok.Data;

@Data
public class RegisterResponse {

	public enum RegisterResponseStatus {
		SUCCESS, NOT_FOUND, VALIDATION_ERROR, EMAIL_NOT_FOUND, AMBIGUOUS, NO_DFV_EMAIL, EMAIL_ALREADY_TAKEN, USER_ALREADY_REGISTERED;
	}

	public RegisterResponseStatus status;
	public String dfvEmail;
	public User user;

	public RegisterResponse(RegisterResponseStatus status) {
		this.status = status;
	}
}

package de.ultical.backend.api.transferClasses;

import de.ultical.backend.model.User;
import lombok.Data;

@Data
public class RegisterResponse {

	public enum RegisterResponseStatus {
		SUCCESS, NOT_FOUND, VALIDATION_ERROR, AMBIGUOUS;
	}

	public RegisterResponse(RegisterResponseStatus status) {
		this.status = status;
	}

	public RegisterResponseStatus status;
	public User user;
}

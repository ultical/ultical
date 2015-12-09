package de.ultical.backend.api.transferClasses;

import java.util.Date;

import lombok.Data;

/**
 * Temporary class to process the register information send by the client
 *
 * @author bas
 *
 */
@Data
public class RegisterRequest {
	private String email;
	private String password;
	private String firstName;
	private String lastName;
	private Date birthDate;
}
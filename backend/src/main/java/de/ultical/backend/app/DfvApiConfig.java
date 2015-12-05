package de.ultical.backend.app;

import lombok.Data;

@Data
public class DfvApiConfig {
	private String token;
	private String secret;
	private String url;
}

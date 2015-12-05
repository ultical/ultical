package de.ultical.backend.app;

import io.dropwizard.Configuration;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UltiCalConfig extends Configuration {
	private DfvApiConfig dfvApi;
}

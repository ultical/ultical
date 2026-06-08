package de.ultical.backend.api;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/version")
public class VersionResource {

    /**
     * The currently deployed application version. Bump this manually in code
     * before each release so the running deployment can be verified.
     */
    public static final String VERSION = "1.1.0";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getVersion() {
        return Collections.singletonMap("version", VERSION);
    }
}

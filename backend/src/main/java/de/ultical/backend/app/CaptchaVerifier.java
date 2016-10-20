package de.ultical.backend.app;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.hk2.api.ServiceLocator;

public class CaptchaVerifier {
    @Inject
    private Client client;

    @Inject
    private UltiCalConfig config;

    public static CaptchaVerifier getInstance() {
        ServiceLocator sl = ServiceLocatorProvider.getInstance().getServiceLocator();
        return sl.createAndInitialize(CaptchaVerifier.class);
    }

    public boolean verifyCaptcha(String captchaResponse) {
        MultivaluedMap<String, String> postData = new MultivaluedHashMap<String, String>();
        postData.add("secret", this.config.getReCaptcha().getSecret());
        postData.add("response", captchaResponse);

        WebTarget target = this.client.target(this.config.getReCaptcha().getUrl());

        Map<String, String> responseData = target.request(MediaType.APPLICATION_JSON).post(Entity.form(postData),
                new GenericType<Map<String, String>>() {
                });

        return responseData.get("success").equalsIgnoreCase("true");
    }
}

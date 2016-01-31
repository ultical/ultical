package de.ultical.backend.app.dfv;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import de.ultical.backend.app.RegistrationHandler;

public class DfvBinder extends AbstractBinder {

    @Override
    protected void configure() {
        this.bind(DfvRegistrationHandler.class).to(RegistrationHandler.class).ranked(5);

    }

}

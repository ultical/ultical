package de.ultical.backend.api;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.mindrot.jbcrypt.BCrypt;

import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.app.EmailCodeService;
import de.ultical.backend.app.MailClient;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.MailCode;
import de.ultical.backend.model.User;

/**
 * Handle mail code link clicks (referred by the frontend)
 *
 * @author bas
 *
 */
@Path("/command/mail")
public class MailResource {

    @Inject
    DataStore dataStore;

    @Inject
    MailClient mailClient;

    @Inject
    UltiCalConfig config;

    @Inject
    Client client;

    @GET
    @Path("code/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public MailCode getMailCode(@PathParam("code") String code) throws Exception {

        try (AutoCloseable c = this.dataStore.getClosable()) {

            MailCode mailCode = this.dataStore.getMailCode(code);

            if (mailCode == null) {
                throw new WebApplicationException("Mail Code not valid", Status.NOT_FOUND);
            }

            switch (mailCode.getType()) {
            case FORGOT_PASSWORD:
                return mailCode;
            case CONFIRM_EMAIL:
                mailCode.getUser().setEmailConfirmed(true);
                break;
            case DFV_MAIL_OPT_IN:
                mailCode.getUser().setDfvEmailOptIn(true);
                break;
            }

            // update user
            this.dataStore.updateUserWithoutPassword(mailCode.getUser());

            // delete mail code
            this.dataStore.deleteMailCode(code);

            return mailCode;
        }
    }

    @POST
    @Path("code/{code}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User updatePassword(@PathParam("code") String code, @NotNull User user) throws Exception {

        try (AutoCloseable c = this.dataStore.getClosable()) {

            MailCode mailCode = this.dataStore.getMailCode(code);

            if (mailCode == null) {
                throw new WebApplicationException("Mail Code not valid", Status.NOT_FOUND);
            }

            if (mailCode.getUser().getId() != user.getId()) {
                throw new WebApplicationException("Email Code and User do not match", Status.FORBIDDEN);
            }

            // encode password
            mailCode.getUser().setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(10)));

            // update user
            this.dataStore.update(mailCode.getUser());

            // delete mail code
            this.dataStore.deleteMailCode(code);

            return mailCode.getUser();
        }
    }

    @POST
    @Path("user/confirmation/resend")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean resendConfirmation(@NotNull User loginData) throws Exception {

        try (AutoCloseable c = this.dataStore.getClosable()) {

            User user = this.dataStore.getUserByEmail(loginData.getEmail());

            if (user == null) {
                throw new WebApplicationException("Email not found", Status.NOT_FOUND);
            }
            return new EmailCodeService(this.dataStore, this.config.getFrontendUrl())
                    .sendEmailConfirmMessage(this.mailClient, user);
        }
    }

    @POST
    @Path("user/optin/resend")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean resendOptIn(@NotNull User loginData) throws Exception {

        try (AutoCloseable c = this.dataStore.getClosable()) {

            User user = this.dataStore.getUserByEmail(loginData.getEmail());

            if (user == null) {
                throw new WebApplicationException("Email not found", Status.NOT_FOUND);
            }

            // get dfv data
            WebTarget target = this.client.target(this.config.getDfvApi().getUrl()).path("profil")
                    .path(String.valueOf(user.getDfvPlayer().getDfvNumber()))
                    .queryParam("token", this.config.getDfvApi().getToken())
                    .queryParam("secret", this.config.getDfvApi().getSecret());

            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            DfvMvPlayer player = invocationBuilder.get(DfvMvPlayer.class);

            return new EmailCodeService(this.dataStore, this.config.getFrontendUrl())
                    .sendEmailDfvOptInMessage(this.mailClient, user, player.getEmail());
        }
    }

    @POST
    @Path("user/password/resend")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean sendForgotPassword(@NotNull User loginData) throws Exception {

        try (AutoCloseable c = this.dataStore.getClosable()) {

            User user = this.dataStore.getUserByEmail(loginData.getEmail());

            if (user == null) {
                throw new WebApplicationException("Email not found", Status.NOT_FOUND);
            }

            user.setPassword("");

            return new EmailCodeService(this.dataStore, this.config.getFrontendUrl())
                    .sendForgotPasswordMessage(this.mailClient, user);
        }
    }
}

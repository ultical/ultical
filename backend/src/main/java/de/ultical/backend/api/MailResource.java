package de.ultical.backend.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import de.ultical.backend.app.Authenticator;
import de.ultical.backend.app.CaptchaVerifier;
import de.ultical.backend.app.EmailCodeService;
import de.ultical.backend.app.MailClient;
import de.ultical.backend.app.MailClient.UlticalMessage.Recipient;
import de.ultical.backend.app.MailClient.UlticalMessage.UlticalRecipientType;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.app.mail.UserMessage;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.MailCode;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.User;
import io.dropwizard.auth.Auth;

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

    @SuppressWarnings("unchecked")
    @POST
    @Path("teams")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean sendEmailToTeamsOfEdition(Map<String, Object> emailInfo, @Auth @NotNull User currentUser)
            throws Exception {

        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {

            TournamentEdition edition;
            if (emailInfo.containsKey("editionId")) {
                Integer editionId = (Integer) emailInfo.get("editionId");
                edition = this.dataStore.get(editionId, TournamentEdition.class);
                Authenticator.assureFormatAdmin(edition.getTournamentFormat(), currentUser);
            } else {
                Integer eventId = (Integer) emailInfo.get("eventId");
                Event event = this.dataStore.get(eventId, Event.class);
                Authenticator.assureEventAdmin(event, currentUser);
                edition = event.getTournamentEdition();
            }

            List<Team> teams = this.dataStore.getTeamsByEditionDivisionsStatus(edition.getId(),
                    (List<Integer>) emailInfo.get("divisions"), (List<String>) emailInfo.get("status"));

            // get all team admins and email addresses
            List<Recipient> recipients = new ArrayList<Recipient>();
            for (Team team : teams) {
                if (team.getContactEmail() != null && !team.getContactEmail().isEmpty()) {
                    recipients.add(new Recipient(team.getContactEmail()));
                }
                for (String email : team.getEmails().split(",")) {
                    recipients.add(new Recipient(email));
                }
                for (User admin : team.getAdmins()) {
                    recipients.add(new Recipient(admin.getEmail(), admin.getFullName()));
                }
            }

            UserMessage message = this.prepareUserMessage(emailInfo, currentUser);

            message.addRecipients(UlticalRecipientType.BCC, recipients);

            if (!this.mailClient.sendMail(message)) {
                throw new WebApplicationException("Error sending Mail", Status.INTERNAL_SERVER_ERROR);
            }
        }
        return true;
    }

    /**
     * Prepare the message with the standard parameters
     *
     * @param emailInfo
     *            A map with stored parameters
     * @param currentUser
     *            The user currently logged in
     * @return UserMessage object
     * @throws Exception
     */
    private UserMessage prepareUserMessage(Map<String, Object> emailInfo, User currentUser) throws Exception {

        if (currentUser == null && !CaptchaVerifier.getInstance().verifyCaptcha((String) emailInfo.get("captcha"))) {
            throw new CaptchaFailedException();
        }

        UserMessage message = new UserMessage();

        message.setSubject((String) emailInfo.get("subject"));
        message.setBody((String) emailInfo.get("body"));
        message.setAuthorDescriptionText((String) emailInfo.get("authorDescriptionText"));

        Recipient currentUserRecipient;
        if (currentUser == null) {
            currentUserRecipient = new Recipient((String) emailInfo.get("replyTo"), (String) emailInfo.get("name"));
        } else {
            currentUserRecipient = new Recipient((String) emailInfo.get("replyTo"), currentUser.getFullName());
        }
        message.setAuthor(currentUserRecipient);
        message.addRecipient(UlticalRecipientType.TO, currentUserRecipient);
        message.addRecipient(UlticalRecipientType.REPLY_TO, currentUserRecipient);

        return message;
    }

    public class CaptchaFailedException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    @POST
    @Path("team")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean sendEmailToTeam(Map<String, Object> emailInfo, @Auth @NotNull User currentUser) throws Exception {
        return this.sendEmailToTeamHelper(emailInfo, currentUser);
    }

    @POST
    @Path("team/ano")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean sendEmailToTeamAno(Map<String, Object> emailInfo) throws Exception {
        return this.sendEmailToTeamHelper(emailInfo, null);
    }

    private boolean sendEmailToTeamHelper(Map<String, Object> emailInfo, User currentUser) throws Exception {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {

            Team team = this.dataStore.get((Integer) emailInfo.get("teamId"), Team.class);

            List<Recipient> recipients = new ArrayList<Recipient>();
            if (!team.getContactEmail().isEmpty()) {
                recipients.add(new Recipient(team.getContactEmail()));
            }
            for (String email : team.getEmails().split(",")) {
                recipients.add(new Recipient(email));
            }
            for (User admin : team.getAdmins()) {
                recipients.add(new Recipient(admin.getEmail(), admin.getFullName()));
            }

            UserMessage message = this.prepareUserMessage(emailInfo, currentUser);

            message.addRecipients(UlticalRecipientType.BCC, recipients);

            if (!this.mailClient.sendMail(message)) {
                throw new WebApplicationException("Error sending Mail", Status.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            throw new WebApplicationException(500);
        }
        return true;
    }

    @POST
    @Path("event")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean sendEmailToEvent(Map<String, Object> emailInfo, @Auth @NotNull User currentUser) throws Exception {
        return this.sendEmailToEventHelper(emailInfo, currentUser);
    }

    @POST
    @Path("event/ano")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean sendEmailToEventAno(Map<String, Object> emailInfo) throws Exception {
        return this.sendEmailToEventHelper(emailInfo, null);
    }

    private boolean sendEmailToEventHelper(Map<String, Object> emailInfo, User currentUser) throws Exception {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {

            Event event = this.dataStore.get((Integer) emailInfo.get("eventId"), Event.class);

            List<Recipient> recipients = new ArrayList<Recipient>();
            if (event.getLocalOrganizer() != null && !event.getLocalOrganizer().getEmail().isEmpty()) {
                recipients
                        .add(new Recipient(event.getLocalOrganizer().getEmail(), event.getLocalOrganizer().getName()));
            }
            for (User admin : event.getAdmins()) {
                recipients.add(new Recipient(admin.getEmail(), admin.getFullName()));
            }
            UserMessage message = this.prepareUserMessage(emailInfo, currentUser);

            message.addRecipients(UlticalRecipientType.BCC, recipients);

            if (!this.mailClient.sendMail(message)) {
                throw new WebApplicationException("Error sending Mail", Status.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + e.getStackTrace());
            throw new WebApplicationException(500);
        }
        return true;
    }
}

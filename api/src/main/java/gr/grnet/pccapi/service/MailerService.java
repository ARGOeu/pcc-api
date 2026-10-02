package gr.grnet.pccapi.service;

import gr.grnet.pccapi.enums.InvitationStatus;
import gr.grnet.pccapi.enums.MailType;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class MailerService {

    private static final Logger LOG = Logger.getLogger(MailerService.class);

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "api.server.url")
    String serviceUrl;

    @ConfigProperty(name = "quarkus.mailer.from")
    String sendFrom;

    @ConfigProperty(name = "quarkus.mailer.reply-to")
    String replyTo;

    @ConfigProperty(name = "api.mail.title", defaultValue = "PID Central Catalog")
    String title;

    @Inject
    @Location("prefix_account_invitation.html")
    Template prefixInvitationTemplate;

    @Inject
    @Location("prefix_account_invitation_response.html")
    Template prefixInvitationResponseTemplate;

    @Inject
    @Location("prefix_account_invitation_accepted.html")
    Template prefixInvitationAcceptedTemplate;


    public void sendPrefixInvitationEmail(List<String> recipientEmails, String providerName, String prefixName, String role, String invitationUrl) {

        HashMap<String, Object> params = new HashMap<>();

        params.put("sendFrom", sendFrom);
        params.put("replyTo", replyTo);
        params.put("title", title);
        params.put("logoUrl", serviceUrl + "/images/logo.png");
        params.put("providerName", providerName);
        params.put("prefixName", prefixName);
        params.put("role", role);
        params.put("invitationUrl", invitationUrl);

        sendBcc(prefixInvitationTemplate, params, MailType.PREFIX_INVITATION_CREATED, recipientEmails);
    }


    public void sendInvitationAcceptedToInvitee(List<String> recipientEmails, String providerName, String prefixName, String role, String prefixDetailsUrl) {

        HashMap<String, Object> params = new HashMap<>();

        params.put("logoUrl", serviceUrl + "/images/logo.png");
        params.put("sendFrom", sendFrom);
        params.put("replyTo", replyTo);
        params.put("title", title);
        params.put("providerName", providerName);
        params.put("prefixName", prefixName);
        params.put("role", role);
        params.put("prefixDetailsUrl", prefixDetailsUrl);

        sendBcc(prefixInvitationAcceptedTemplate, params, MailType.PREFIX_INVITATION_RESPONSE_NOTIFY_USER, recipientEmails);
    }


    public void sendInvitationResponseToAdmins(List<String> adminEmails, String providerName, String prefixName, String inviteeEmail, String role, InvitationStatus status, String prefixAccountsUrl) {

        HashMap<String, Object> params = new HashMap<>();

        params.put("logoUrl", serviceUrl + "/images/logo.png");
        params.put("sendFrom", sendFrom);
        params.put("replyTo", replyTo);
        params.put("title", title);
        params.put("providerName", providerName);
        params.put("prefixName", prefixName);
        params.put("inviteeEmail", inviteeEmail);
        params.put("role", role);
        params.put("status", status.name());
        params.put("prefixAccountsUrl", prefixAccountsUrl);

        sendBcc(prefixInvitationResponseTemplate, params, MailType.PREFIX_INVITATION_RESPONSE_NOTIFY_ADMIN, adminEmails
        );
    }


    private void sendBcc(Template template, HashMap<String, Object> params, MailType mailType, List<String> recipients) {

        if (recipients == null || recipients.isEmpty()) {
            LOG.warnf("Email not sent: no recipients for mail type %s", mailType);
            return;
        }

        var mailTemplate = mailType.execute(template, params);

        var mail = new Mail();
        mail.setFrom(sendFrom);
        mail.setReplyTo(replyTo);
        mail.setBcc(recipients);
        mail.setSubject(mailTemplate.getSubject());
        mail.setHtml(mailTemplate.getBody());

        try {
            mailer.send(mail);

            LOG.infof(
                    "Email sent to %s (type=%s, subject=%s)",
                    recipients,
                    mailType,
                    mail.getSubject()
            );

        } catch (Exception e) {

            LOG.errorf(
                    e,
                    "Failed to send email to %s (type=%s, subject=%s)",
                    recipients,
                    mailType,
                    mail.getSubject()
            );

            throw e;
        }
    }
}
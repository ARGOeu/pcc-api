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
    @Location("invitation.html")
    Template invitationTemplate;

    @Inject
    @Location("invitation_response.html")
    Template invitationResponseTemplate;

    @Inject
    @Location("invitation_accepted.html")
    Template invitationAcceptedTemplate;

    public void sendInvitationEmail(List<String> recipientEmails, String targetType, String targetName, String parentType, String parentName, String role, String invitationUrl) {

        var params = commonParams();

        params.put("targetType", targetType);
        params.put("targetName", targetName);
        params.put("parentType", parentType);
        params.put("parentName", parentName);
        params.put("role", role);
        params.put("invitationUrl", invitationUrl);

        sendBcc(invitationTemplate, params, MailType.INVITATION_CREATED, recipientEmails);
    }

    public void sendInvitationAcceptedToInvitee(List<String> recipientEmails, String targetType, String targetName, String parentType, String parentName, String role, String targetDetailsUrl) {

        var params = commonParams();

        params.put("targetType", targetType);
        params.put("targetName", targetName);
        params.put("parentType", parentType);
        params.put("parentName", parentName);
        params.put("role", role);
        params.put("targetDetailsUrl", targetDetailsUrl);

        sendBcc(invitationAcceptedTemplate, params, MailType.INVITATION_RESPONSE_NOTIFY_USER, recipientEmails);
    }

    public void sendInvitationResponseToAdmins(List<String> adminEmails, String targetType, String targetName, String parentType, String parentName, String inviteeEmail, String role, InvitationStatus status, String targetMembersUrl) {

        var params = commonParams();

        params.put("targetType", targetType);
        params.put("targetName", targetName);
        params.put("parentType", parentType);
        params.put("parentName", parentName);
        params.put("inviteeEmail", inviteeEmail);
        params.put("role", role);
        params.put("status", status.name());
        params.put("targetMembersUrl", targetMembersUrl);

        sendBcc(invitationResponseTemplate, params, MailType.INVITATION_RESPONSE_NOTIFY_ADMIN, adminEmails);
    }

    private HashMap<String, Object> commonParams() {

        var params = new HashMap<String, Object>();

        params.put("logoUrl", serviceUrl + "/images/logo.png");
        params.put("sendFrom", sendFrom);
        params.put("replyTo", replyTo);
        params.put("title", title);

        return params;
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
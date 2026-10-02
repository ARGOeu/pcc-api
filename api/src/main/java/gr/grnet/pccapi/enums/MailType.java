package gr.grnet.pccapi.enums;

import io.quarkus.qute.Template;

import java.util.HashMap;

public enum MailType {

    PREFIX_INVITATION_CREATED {
        @Override
        public MailTemplate execute(
                Template emailTemplate,
                HashMap<String, Object> templateParams) {

            var body = emailTemplate
                    .data("logoUrl", templateParams.get("logoUrl"))
                    .data("title", templateParams.get("title"))
                    .data("sendFrom", templateParams.get("sendFrom"))
                    .data("replyTo", templateParams.get("replyTo"))

                    .data("recipientName", templateParams.get("recipientName"))
                    .data("providerName", templateParams.get("providerName"))
                    .data("prefixName", templateParams.get("prefixName"))
                    .data("role", templateParams.get("role"))
                    .data("invitationUrl", templateParams.get("invitationUrl"))
                    .render();

            var subject = "[" + templateParams.get("title")
                    + "] Invitation to join prefix "
                    + templateParams.get("prefixName")
                    + " – "
                    + templateParams.get("providerName");

            return new MailTemplate(subject, body);
        }
    },

    PREFIX_INVITATION_RESPONSE_NOTIFY_USER {
        @Override
        public MailTemplate execute(
                Template emailTemplate,
                HashMap<String, Object> templateParams) {

            var body = emailTemplate
                    .data("logoUrl", templateParams.get("logoUrl"))
                    .data("title", templateParams.get("title"))
                    .data("sendFrom", templateParams.get("sendFrom"))
                    .data("replyTo", templateParams.get("replyTo"))

                    .data("providerName", templateParams.get("providerName"))
                    .data("prefixName", templateParams.get("prefixName"))
                    .data("role", templateParams.get("role"))
                    .data("prefixDetailsUrl", templateParams.get("prefixDetailsUrl"))
                    .render();

            var subject = "[" + templateParams.get("title")
                    + "] Invitation accepted – access granted to "
                    + templateParams.get("prefixName");

            return new MailTemplate(subject, body);
        }
    },

    PREFIX_INVITATION_RESPONSE_NOTIFY_ADMIN {
        @Override
        public MailTemplate execute(
                Template emailTemplate,
                HashMap<String, Object> templateParams) {

            var body = emailTemplate
                    .data("logoUrl", templateParams.get("logoUrl"))
                    .data("title", templateParams.get("title"))
                    .data("sendFrom", templateParams.get("sendFrom"))
                    .data("replyTo", templateParams.get("replyTo"))

                    .data("providerName", templateParams.get("providerName"))
                    .data("prefixName", templateParams.get("prefixName"))
                    .data("inviteeEmail", templateParams.get("inviteeEmail"))
                    .data("role", templateParams.get("role"))
                    .data("status", templateParams.get("status"))
                    .data("prefixDetailsUrl", templateParams.get("prefixDetailsUrl"))
                    .render();

            var subject = "[" + templateParams.get("title")
                    + "] Invitation "
                    + templateParams.get("status")
                    + " – "
                    + templateParams.get("prefixName");

            return new MailTemplate(subject, body);
        }
    },

    PREFIX_ACCESS_GRANTED_USER {
        @Override
        public MailTemplate execute(
                Template emailTemplate,
                HashMap<String, Object> templateParams) {

            var body = emailTemplate
                    .data("logoUrl", templateParams.get("logoUrl"))
                    .data("title", templateParams.get("title"))
                    .data("replyTo", templateParams.get("replyTo"))

                    .data("providerName", templateParams.get("providerName"))
                    .data("prefixName", templateParams.get("prefixName"))
                    .data("role", templateParams.get("role"))
                    .data("prefixDetailsUrl", templateParams.get("prefixDetailsUrl"))
                    .render();

            var subject = "[" + templateParams.get("title")
                    + "] Welcome! You’ve Been Added to Prefix "
                    + templateParams.get("prefixName");

            return new MailTemplate(subject, body);
        }
    },

    INCIDENT_CREATED {
        @Override
        public MailTemplate execute(
                Template emailTemplate,
                HashMap<String, Object> templateParams) {

            var body = emailTemplate
                    .data("logoUrl", templateParams.get("logoUrl"))
                    .data("title", templateParams.get("title"))
                    .data("replyTo", templateParams.get("replyTo"))

                    .data("incidentNumber", templateParams.get("incidentNumber"))
                    .data("incidentTitle", templateParams.get("incidentTitle"))
                    .data("description", templateParams.get("description"))
                    .data("serviceNames", templateParams.get("serviceNames"))
                    .data("status", templateParams.get("status"))
                    .data("createdBy", templateParams.get("createdBy"))
                    .data("createdAt", templateParams.get("createdAt"))
                    .data("incidentUrl", templateParams.get("incidentUrl"))
                    .render();

            var subject = "[" + templateParams.get("title")
                    + "] New incident reported: "
                    + templateParams.get("incidentNumber");

            return new MailTemplate(subject, body);
        }
    };

    public abstract MailTemplate execute(
            Template mailTemplate,
            HashMap<String, Object> templateParams
    );

    public static class MailTemplate {

        private String subject;
        private String body;

        public MailTemplate(String subject, String body) {
            this.subject = subject;
            this.body = body;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
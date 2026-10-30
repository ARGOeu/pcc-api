package gr.grnet.pccapi.enums.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;

public enum InvitationResource implements ApiResource {

    INVITATION;
    @Override
    public String resourceName() {
        return "Invitation";
    }
}
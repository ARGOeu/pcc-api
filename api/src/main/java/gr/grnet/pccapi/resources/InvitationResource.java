package gr.grnet.pccapi.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;

public enum InvitationResource implements ApiResource {

    INVITATION;
    @Override
    public String resourceName() {
        return "Invitation";
    }
}
package gr.grnet.pccapi.dto.invitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.grnet.pccapi.enums.InvitationAction;
import gr.grnet.pccapi.enums.InvitationStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;


public class InvitationActionResponse {

    @Schema(
            type = SchemaType.OBJECT,
            implementation = InvitationStatus.class,
            description = "Invitation action",
            enumeration = {"ACCEPT", "REJECT"},
            example = "ACCEPT"
    )
    @NotNull
    public InvitationAction action;
}

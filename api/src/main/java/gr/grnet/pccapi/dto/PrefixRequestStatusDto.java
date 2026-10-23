package gr.grnet.pccapi.dto;

import gr.grnet.pccapi.validator.constraints.ValidPrefixRequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
public class PrefixRequestStatusDto {

    @NotNull(message = "Status should be provided")
    @NotBlank(message = "Status should be provided")
    @ValidPrefixRequestStatus
    @Schema(
            description = "The expected status of the prefix request.",
            example = "ACCEPTED"
    )
    public String status;

    @NotNull(message = "Reason should be provided")
    @NotBlank(message = "Reason should be provided")
    @Schema(
            description = "The reason for the assigned status of the prefix request.",
            example = "All requirements approved"
    )
    public String reason;
}
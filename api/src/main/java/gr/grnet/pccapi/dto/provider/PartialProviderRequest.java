package gr.grnet.pccapi.dto.provider;


import com.fasterxml.jackson.annotation.JsonProperty;
import gr.grnet.pccapi.dto.ContactDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(name = "PartialProviderRequest", description = "Represents the configuration of an update on provider.")
@Getter
@Setter
public class PartialProviderRequest {



    @JsonProperty("contacts")
    @NotEmpty(message = "At least one contact should be added")
    @Valid
    public List<ContactDto> contacts;


}
package gr.grnet.pccapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignProviderDto {

    @NotNull(message = "Provider ID must not be empty")
    public Integer providerId;
}

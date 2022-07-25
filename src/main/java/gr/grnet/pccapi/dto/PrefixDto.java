package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Accessors(chain = true)
@Schema(name = "Prefix")
public class PrefixDto {

  @NotNull public String name;
  @NotNull public String owner;

  @JsonProperty("used_by")
  public String usedBy;

  @NotNull public Integer status;

  @Schema(
      type = SchemaType.INTEGER,
      implementation = Integer.class,
      description = "The unique service ID to be linked with the prefix.",
      example = "1")
  @JsonProperty("service_id")
  @NotNull
  public Integer serviceId;

  @Schema(
      type = SchemaType.INTEGER,
      implementation = Integer.class,
      description = "The unique domain ID to be linked with the prefix.",
      example = "1")
  @JsonProperty("domain_id")
  public Integer domainId;

  @NotNull
  @Schema(
      type = SchemaType.INTEGER,
      implementation = Integer.class,
      description = "The unique provider ID to be linked with the prefix.",
      example = "1")
  @JsonProperty("provider_id")
  public Integer providerId;
}

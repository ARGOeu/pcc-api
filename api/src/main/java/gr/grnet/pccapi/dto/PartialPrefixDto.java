package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.grnet.pccapi.enums.LookUpServiceType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Accessors(chain = true)
@Schema(name = "PartialPrefix")
public class PartialPrefixDto {

  public String name;

  public String owner;

  @JsonProperty("used_by")
  public String usedBy;

  public String status;

  @Schema(
      type = SchemaType.INTEGER,
      implementation = Integer.class,
      description = "The unique service ID to be linked with the prefix.",
      example = "1")
  @JsonProperty("service_id")
  public Integer serviceId;

  @Schema(
      type = SchemaType.STRING,
      implementation = LookUpServiceType.class,
      description = "The type of lookup service the prefix supports",
      example = "PRIVATE")
  @JsonProperty("lookup_service_type")
  public LookUpServiceType lookUpServiceType;

  @Schema(
      type = SchemaType.INTEGER,
      implementation = Integer.class,
      description = "The unique domain ID to be linked with the prefix.",
      example = "1")
  @JsonProperty("domain_id")
  public Integer domainId;

  @Schema(
      type = SchemaType.INTEGER,
      implementation = Integer.class,
      description = "The unique provider ID to be linked with the prefix.",
      example = "1")
  @JsonProperty("provider_id")
  public Integer providerId;

  @JsonProperty("resolvable")
  public Boolean resolvable = Boolean.TRUE;
}

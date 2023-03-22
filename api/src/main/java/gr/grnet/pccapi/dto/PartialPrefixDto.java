package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.grnet.pccapi.enums.ContractType;
import gr.grnet.pccapi.enums.LookUpServiceType;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
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

  @JsonProperty("contract_end")
  public String contractEnd;

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
  public String lookUpServiceType;

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

  @JsonProperty("contact_name")
  public String contactName;

  @Email(
      regexp =
          "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$",
      flags = Pattern.Flag.CASE_INSENSITIVE)
  @JsonProperty("contact_email")
  public String contactEmail;

  @Schema(
      type = SchemaType.STRING,
      implementation = ContractType.class,
      description = "The type of contract type",
      example = "Project")
  @JsonProperty("contract_type")
  public String contractType;
}

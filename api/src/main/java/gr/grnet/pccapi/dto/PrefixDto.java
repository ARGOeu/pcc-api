package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.grnet.pccapi.enums.ContractType;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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

  @JsonProperty("contract_end")
  public String contractEnd;

  public Integer status;

  @Schema(
      type = SchemaType.INTEGER,
      implementation = Integer.class,
      description = "The unique lookup service ID to be linked with the prefix.",
      example = "1")
  @JsonProperty("lookup_service_type")
  @NotNull
  public Integer lookUpServiceType;

  @Schema(
      type = SchemaType.INTEGER,
      implementation = Integer.class,
      description = "The unique service ID to be linked with the prefix.",
      example = "1")
  @JsonProperty("service_id")
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

  @JsonProperty("resolvable")
  public Boolean resolvable = Boolean.TRUE;

  @JsonProperty("contact_name")
  @NotNull
  public String contactName;

  @Email(
      regexp =
          "^$|(^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$)",
      flags = Pattern.Flag.CASE_INSENSITIVE)
  @JsonProperty("contact_email")
  @NotNull
  public String contactEmail;

  @Schema(
      type = SchemaType.STRING,
      implementation = ContractType.class,
      description = "The type of contract type",
      example = "Project")
  @JsonProperty("contract_type")
  @NotNull
  public String contractType;
}

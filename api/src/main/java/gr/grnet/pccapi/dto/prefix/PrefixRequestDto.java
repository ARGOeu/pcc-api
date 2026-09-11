package gr.grnet.pccapi.dto.prefix;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.grnet.pccapi.resolvers.CheckDateFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Accessors(chain = true)
@Schema(name = "Prefix")
public class PrefixRequestDto {

  @NotNull
  @Schema(
          type = SchemaType.STRING,
          implementation = String.class,
          description = "The unique name of the prefix.",
          example = "21.T227")
  public String name;

  @NotNull
  @Schema(
          type = SchemaType.STRING,
          implementation = String.class,
          description = "The owner of the prefix.",
          example = "GRNET")
  public String owner;

  @Schema(
          type = SchemaType.STRING,
          implementation = String.class,
          description = "The entity or organization using the prefix.",
          example = "GRNET")
  @JsonProperty("used_by")
  public String usedBy;

  @Schema(
          type = SchemaType.STRING,
          implementation = String.class,
          description = "The contract end date in yyyy-MM-dd format.",
          example = "2028-07-22")
  @JsonProperty("contract_end")
  @CheckDateFormat(
          pattern = "yyyy-MM-dd",
          message = "Valid date format is yyyy-MM-dd.")
  public String contractEnd;

  @Schema(
          type = SchemaType.INTEGER,
          implementation = Integer.class,
          description = "The status of the prefix.",
          example = "0")
  public Integer status;

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

  @Schema(
          type = SchemaType.BOOLEAN,
          implementation = Boolean.class,
          description = "Indicates whether the prefix is resolvable.",
          example = "true")
  @JsonProperty("resolvable")
  public Boolean resolvable = Boolean.TRUE;

  @NotNull
  @Schema(
          type = SchemaType.STRING,
          implementation = String.class,
          description = "The name of the contact person for the prefix.",
          example = "John Doe")
  @JsonProperty("contact_name")
  public String contactName;

  @Email(
          regexp =
                  "^$|(^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$)",
          flags = Pattern.Flag.CASE_INSENSITIVE)
  @NotNull
  @Schema(
          type = SchemaType.STRING,
          implementation = String.class,
          description = "The email address of the contact person for the prefix.",
          example = "contact@example.org")
  @JsonProperty("contact_email")
  public String contactEmail;

  @Schema(
          type = SchemaType.INTEGER,
          implementation = Integer.class,
          description = "The unique contract type ID to be linked with the prefix.",
          example = "5")
  @JsonProperty("contract_type_id")
  public Integer contractTypeId;

  @Schema(
          type = SchemaType.INTEGER,
          implementation = Integer.class,
          description = "The unique lookup service type ID to be linked with the prefix.",
          example = "1")
  @JsonProperty("lookup_service_type_id")
  public Integer lookUpServiceTypeId;
}
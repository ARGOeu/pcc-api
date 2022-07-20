package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PrefixDto {

    public Integer id;
    public String name;
    public String owner;
    @JsonProperty("used_by")
    public String usedBy;
    public Integer status;

    @JsonProperty("service_id")
    public Integer serviceId;
    @JsonProperty("service_name")
    public String serviceName;

    @JsonProperty("domain_id")
    public Integer domainId;
    @JsonProperty("domain_name")
    public String domainName;

    @JsonProperty("provider_id")
    public Integer providerId;
    @JsonProperty("provider_name")
    public String providerName;
}

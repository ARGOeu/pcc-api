package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DomainDto {

    public Integer id;
    @JsonProperty("domain_id")
    public String domainId;
    public String name;
    public String description;
}

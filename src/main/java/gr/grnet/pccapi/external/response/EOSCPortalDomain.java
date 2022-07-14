package gr.grnet.pccapi.external.response;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EOSCPortalDomain {

    @EqualsAndHashCode.Include
    public String id;
    @EqualsAndHashCode.Include
    public String name;
    @EqualsAndHashCode.Include
    public String description;
}
package gr.grnet.pccapi.entity;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
public class ProviderContactJunction {

    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    public Integer id;

    public String name;
    public String email;
    public String type;
    public List<Provider> providers;

    public ProviderContactJunction(
            Integer id,
            String name,
            String email,
            String type,
            List<Provider> providers) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.type = type;
        this.providers = providers;
    }

    public ProviderContactJunction(Contact contact, List<Provider> providers) {
        this.id = contact.getId();
        this.name = contact.getContactName();
        this.email = contact.getContactEmail();
        this.type = contact.getContactType().name();
        this.providers = providers;
    }
}


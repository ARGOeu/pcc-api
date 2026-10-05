package gr.grnet.pccapi.enums.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;

public enum PrefixResource implements ApiResource {

    PREFIX;
    @Override
    public String resourceName() {
        return "Prefix";
    }
}


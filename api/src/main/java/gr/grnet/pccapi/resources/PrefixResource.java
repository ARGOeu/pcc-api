package gr.grnet.pccapi.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;

public enum PrefixResource implements ApiResource {

    PREFIX_RESOURCE;
    @Override
    public String resourceName() {
        return "Prefix";
    }
}
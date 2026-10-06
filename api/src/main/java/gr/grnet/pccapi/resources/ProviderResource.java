package gr.grnet.pccapi.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;

public enum ProviderResource implements ApiResource {

    PROVIDER;
    @Override
    public String resourceName() { return "Provider"; }
}
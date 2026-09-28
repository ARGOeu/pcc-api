package gr.grnet.pccapi.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;

public class ProviderResource implements ApiResource {

    @Override
    public String resourceName() {
        return "Provider";
    }
}

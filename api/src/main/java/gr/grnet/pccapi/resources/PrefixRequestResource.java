package gr.grnet.pccapi.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;

public enum PrefixRequestResource implements ApiResource {

    PREFIXREQUEST;
    @Override
    public String resourceName() { return "PrefixRequest"; }
}

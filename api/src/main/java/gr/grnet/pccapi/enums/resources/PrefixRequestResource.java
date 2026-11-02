package gr.grnet.pccapi.enums.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;

public enum PrefixRequestResource implements ApiResource {

    PREFIXREQUEST;
    @Override
    public String resourceName() { return "PrefixRequest"; }
}

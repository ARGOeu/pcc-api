package gr.grnet.pccapi.templates;

import io.quarkus.qute.Template;
import io.quarkus.vertx.web.Route;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class LandingRoute {

    @Inject
    Template index;

    @ConfigProperty(name = "api.html.oidc.client.url")
    String apiHtmlOidcClientUrl;

    @ConfigProperty(name = "api.html.swagger.documentation")
    String apiHtmlSwaggerDocumentation;


    @Route(path = "/", methods = Route.HttpMethod.GET)
    public void landing(RoutingContext rc) {
        rc
                .response()
                .putHeader("content-type", "text/html")
                .end(index.data("pcc_api_oidc_client_url", apiHtmlOidcClientUrl, "pcc_api_documentation", apiHtmlSwaggerDocumentation).render());
    }
}
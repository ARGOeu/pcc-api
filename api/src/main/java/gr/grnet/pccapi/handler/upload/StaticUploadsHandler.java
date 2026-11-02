package gr.grnet.pccapi.handler.upload;

import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Singleton
public class StaticUploadsHandler {

    private static final Logger LOG = Logger.getLogger(StaticUploadsHandler.class);

    @ConfigProperty(name = "base.upload.logo.dir")
    String baseUploadLogoDir;

    void onStart(@Observes StartupEvent ev, Router router) {
        LOG.infof(" Serving /logos/* from directory: %s", baseUploadLogoDir);

        router.route("/logos/*").handler(
                StaticHandler.create(FileSystemAccess.ROOT, baseUploadLogoDir)
                        .setCachingEnabled(true)
                        .setIncludeHidden(false)
                        .setDefaultContentEncoding("UTF-8")
        );
    }

}

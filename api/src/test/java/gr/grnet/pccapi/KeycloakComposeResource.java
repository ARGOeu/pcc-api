package gr.grnet.pccapi;


import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


public class KeycloakComposeResource implements QuarkusTestResourceLifecycleManager {

    private static final String REALM_NAME = "rciam";
    private static final String DB_NAME = "postgres2";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";
    private static final String PARENT_GROUP = "pcc-api";
    private static final String SUPER_ADMIN_ROLE = "super_admin";


    private static final Path KEYCLOAK_MODULE_DIR = resolveKeycloakModuleDir();


    private static final ImageFromDockerfile KEYCLOAK_IMAGE =
            new ImageFromDockerfile("test1_keycloak", false)
                    .withDockerfile(KEYCLOAK_MODULE_DIR.resolve("Dockerfile"));

    private Network network;
    private PostgreSQLContainer<?> postgres;
    private GenericContainer<?> keycloak;

    @Override
    public Map<String, String> start() {
        network = Network.newNetwork();

        postgres = new PostgreSQLContainer<>("postgres:17")
                .withNetwork(network)
                .withNetworkAliases("postgres")
                .withDatabaseName(DB_NAME)
                .withUsername(DB_USER)
                .withPassword(DB_PASSWORD);
        postgres.start();

        keycloak = new GenericContainer<>(KEYCLOAK_IMAGE)
                .withNetwork(network)
                .withNetworkAliases("keycloak")
                .withExposedPorts(8080)
                .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "tempadmin")
                .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "tempadmin")
                .withEnv("KC_DB", "postgres")
                .withEnv("KC_DB_URL", "jdbc:postgresql://postgres:5432/" + DB_NAME)
                .withEnv("KC_DB_USERNAME", DB_USER)
                .withEnv("KC_DB_PASSWORD", DB_PASSWORD)
                .withEnv("KC_HEALTH_ENABLED", "true")
                .withEnv("KC_METRICS_ENABLED", "true")
                .withCommand("start-dev", "--http-port=8080", "--import-realm")
                .withFileSystemBind(
                        KEYCLOAK_MODULE_DIR.resolve("realms").toString(),
                        "/opt/keycloak/data/import",
                        BindMode.READ_ONLY)
                .waitingFor(Wait.forHttp("/realms/master/.well-known/openid-configuration")
                        .forPort(8080)
                        .withStartupTimeout(Duration.ofSeconds(180)));

        keycloak.start();

        runEntitlements();

        Map<String, String> conf = new HashMap<>();
        String authServerUrl = String.format("http://%s:%d/realms/%s",
                keycloak.getHost(), keycloak.getMappedPort(8080), REALM_NAME);
        conf.put("quarkus.oidc.auth-server-url", authServerUrl);
        conf.put(
                "api.auth.entitlements.keycloak-group-management-client-url",
                String.format(
                        "http://%s:%d/realms/rciam/",
                        keycloak.getHost(),
                        keycloak.getMappedPort(8080)
                )
        );

        KeycloakTestClient.init(authServerUrl);

        return conf;
    }

    @Override
    public void inject(Object testInstance) {
        Class<?> current = testInstance.getClass();
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(KeycloakToken.class) && field.getType() == String.class) {
                    KeycloakToken meta = field.getAnnotation(KeycloakToken.class);
                    String token = KeycloakTestClient.getAccessToken(meta.username(), meta.password());
                    field.setAccessible(true);
                    try {
                        field.set(testInstance, token);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to inject Keycloak token into field " + field.getName(), e);
                    }
                }
            }
            current = current.getSuperclass();
        }
    }

    private void runEntitlements() {

        GenericContainer<?> entitlements = new GenericContainer<>(KEYCLOAK_IMAGE)
                .withNetwork(network)
                .withEnv("KC_DB", "postgres")
                .withEnv("KC_DB_URL", "jdbc:postgresql://postgres:5432/" + DB_NAME)
                .withEnv("KC_DB_USERNAME", DB_USER)
                .withEnv("KC_DB_PASSWORD", DB_PASSWORD)
                .withEnv("API_AUTH_ENTITLEMENTS_PARENT_GROUP", PARENT_GROUP)
                .withEnv("API_AUTH_ENTITLEMENTS_SUPER_ADMIN_ROLE", SUPER_ADMIN_ROLE)
                .withCopyFileToContainer(
                        MountableFile.forHostPath(KEYCLOAK_MODULE_DIR.resolve("init.sh")),
                        "/tmp/init.sh")
                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("sh", "/tmp/init.sh"))
                .withStartupCheckStrategy(
                        new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(180)));
        try {
            entitlements.start();
        } finally {
            System.out.println("=== entitlements logs ===");
            System.out.println(entitlements.getLogs());
            entitlements.stop();
        }
    }

    private static Path resolveKeycloakModuleDir() {
        String fromSystemProperty = System.getProperty("keycloak.module.dir");
        if (fromSystemProperty != null && !fromSystemProperty.isBlank()) {
            return Paths.get(fromSystemProperty);
        }
        throw new IllegalArgumentException("Define the keycloak folder!");
    }

    @Override
    public void stop() {
        if (keycloak != null) {
            keycloak.stop();
        }
        if (postgres != null) {
            postgres.stop();
        }
        if (network != null) {
            network.close();
        }
    }
}
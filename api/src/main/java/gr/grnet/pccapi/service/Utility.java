package gr.grnet.pccapi.service;

import io.quarkus.oidc.TokenIntrospection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.Math.min;
import static java.util.stream.Collectors.toMap;

/**
 * Utility service providing helper methods for extracting user information and common operations.
 */
@ApplicationScoped
public class Utility {

    @Inject
    TokenIntrospection tokenIntrospection;

    @ConfigProperty(name = "api.auth.oidc.user.unique.id")
    String key;

    /**
     * Retrieves the configured unique user identifier from the OIDC access token.
     *
     * @return user unique identifier
     */public String getUserUniqueIdentifier() {

        String id;

        try {
            id = tokenIntrospection.getJsonObject().getString(key);
        } catch (Exception e) {

            String message = String.format("The User's unique identifier {%s} is missing from the access token.", key);
            throw new BadRequestException(message);
        }

        return id;
    }

    /**
     * Retrieves the username from the access token.
     *
     * @return username
     */
    public String getUsername() {
        try {
            return getUserUniqueIdentifier();
        } catch (Exception e) {
            throw new BadRequestException("Missing 'voperson_id' in access token.");
        }
    }

    /**
     * Retrieves the user's email from the access token.
     *
     * @return user email or null if not present
     */
    public String getUserEmail() {
        return tokenIntrospection.getJsonObject().getString("email", null);
    }

    /**
     * Retrieves the user's given name from the access token.
     *
     * @return user given name or null if not present
     */
    public String getUserName() {
        return tokenIntrospection.getJsonObject().getString("given_name", null);
    }

    /**
     * Retrieves the user's family name from the access token.
     *
     * @return user family name or null if not present
     */
    public String getUserSurname() {
        return tokenIntrospection.getJsonObject().getString("family_name", null);
    }

    /**
     * Retrieves the preferred username (uid) from the access token.
     *
     * @return preferred username or null if not present
     */public String getUid() { return tokenIntrospection.getJsonObject().getString("preferred_username", null);}

    /**
     * Partitions a list into pages of the specified size.
     *
     * @param list list to partition
     * @param pageSize page size
     * @return map of page index to sublist
     */
    public <T> Map<Integer, List<T>> partition(List<T> list, int pageSize) {

        return IntStream.iterate(0, i -> i + pageSize)
                .limit((list.size() + pageSize - 1) / pageSize)
                .boxed()
                .collect(toMap(i -> i / pageSize,
                        i -> list.subList(i, min(i + pageSize, list.size()))));
    }
}

package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.validation.ValidateUrlResponseDto;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@ApplicationScoped
public class ValidationService {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public ValidateUrlResponseDto validateUrl(String url) {

        URI uri;

        try {
            uri = URI.create(url.trim());

            if (uri.getHost() == null
                    || (!"http".equalsIgnoreCase(uri.getScheme())
                    && !"https".equalsIgnoreCase(uri.getScheme()))) {
                return response(false, false, "Invalid URL format");
            }

        } catch (Exception e) {
            return response(false, false, "Invalid URL format");
        }

        try {
            var request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            var result = client.send(request, HttpResponse.BodyHandlers.discarding());

            boolean reachable = result.statusCode() >= 200 && result.statusCode() < 400;

            return response(
                    true,
                    reachable,
                    reachable ? "URL is valid and reachable" : "URL is not reachable"
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return response(true, false, "URL validation interrupted");

        } catch (Exception e) {
            return response(true, false, "URL is not reachable");
        }
    }

    private ValidateUrlResponseDto response(boolean valid, boolean reachable, String message) {
        return new ValidateUrlResponseDto()
                .setValid(valid)
                .setReachable(reachable)
                .setMessage(message);
    }
}
package gr.grnet.pccapi.service;

import gr.grnet.pccapi.client.handle.HandleClient;
import gr.grnet.pccapi.dto.handle.HandleResponseDto;
import gr.grnet.pccapi.mapper.HandleMapper;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.util.List;

@ApplicationScoped
public class HandleCacheService {

    @CacheResult(cacheName = "handle-identifiers")
    public List<String> getIdentifiers(
            @CacheKey String serviceUrl,
            @CacheKey String prefix,
            @CacheKey String authFingerprint,
            String authorization) {

        var client = buildClient(serviceUrl);
        var response = client.getAll(authorization, prefix);

        return List.copyOf(response.getHandles());
    }

    @CacheResult(cacheName = "handle-details")
    public HandleResponseDto getDetails(
            @CacheKey String serviceUrl,
            @CacheKey String identifier,
            @CacheKey String authFingerprint,
            String authorization,
            String prefix,
            String suffix) {

        var client = buildClient(serviceUrl);

        var response = client.getHandle(authorization, prefix, suffix);

        return HandleMapper.INSTANCE.getToResponseDto(response);
    }

    @CacheInvalidate(cacheName = "handle-identifiers")
    public void invalidateIdentifiers(
            @CacheKey String serviceUrl,
            @CacheKey String prefix,
            @CacheKey String authFingerprint) {
    }

    @CacheInvalidate(cacheName = "handle-details")
    public void invalidateDetails(
            @CacheKey String serviceUrl,
            @CacheKey String identifier,
            @CacheKey String authFingerprint) {
    }

    private HandleClient buildClient(String serviceUrl) {
        return QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(serviceUrl))
                .build(HandleClient.class);
    }
}
package com.solab.appdesktop.api;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    private static final Set<String> ALLOWED_ORIGINS = Arrays.stream(AppIntegrationConstants.CORS_ALLOWED_ORIGINS)
            .collect(Collectors.toUnmodifiableSet());

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().add("Vary", "Origin");
        }
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Accept, Origin");
        responseContext.getHeaders().add("Access-Control-Max-Age", "86400");
    }
}

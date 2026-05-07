package com.solab.appdesktop.api;

/**
 * Puertos y orígenes del laboratorio (alinear con {@code simulador-procesos/.env}).
 * <p>
 * App 1 (API XML): host {@link #APP1_API_HOST}, puerto {@link #APP1_API_PORT}.
 * Override JVM: {@code -Dapp.api.host=... -Dapp.api.port=...}
 * <p>
 * Laravel ({@code artisan serve}): {@code http://127.0.0.1:8000} → {@code APP_URL}.
 * Vite: {@code http://localhost:5173} → {@code FRONTEND_URL} / CORS.
 */
public final class AppIntegrationConstants {

    private AppIntegrationConstants() {
    }

    /** Host donde escucha la API XML de App 1. */
    public static final String APP1_API_HOST = "127.0.0.1";

    /** Puerto donde escucha la API XML de App 1 (mismo valor por defecto que {@code APP1_API_URL} en Laravel). */
    public static final int APP1_API_PORT = 8080;

    /**
     * Orígenes CORS permitidos (navegador). Deben coincidir con {@code CORS_ALLOWED_ORIGINS} en
     * {@code simulador-procesos/.env}.
     */
    public static final String[] CORS_ALLOWED_ORIGINS = {
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:8000",
            "http://127.0.0.1:8000",
    };
}

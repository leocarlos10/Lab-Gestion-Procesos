package com.solab.appdesktop.api;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.BindException;
import java.net.URI;

public class ApiHttpServer {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8080;

    private final CatalogoApiService catalogoApiService = new CatalogoApiService();
    private com.sun.net.httpserver.HttpServer server;

    public void start() throws Exception {
        if (server != null) {
            return;
        }

        String host = System.getProperty("app.api.host", DEFAULT_HOST);
        int port = Integer.getInteger("app.api.port", DEFAULT_PORT);
        URI baseUri = URI.create("http://" + host + ":" + port + "/");

        try {
            ResourceConfig config = new ResourceConfig()
                    .register(new CatalogoResource(catalogoApiService))
                    .register(CorsFilter.class);

            server = JdkHttpServerFactory.createHttpServer(baseUri, config, false);
            server.start();

            System.out.println("API REST XML disponible en " + baseUri + "api/catalogos");
        } catch (Exception e) {
            if (causedByBindException(e)) {
                throw new IllegalStateException(
                        "No se pudo iniciar la API REST porque el puerto " + port + " ya esta en uso. "
                                + "Configure otro con -Dapp.api.port=<puerto> o cierre el proceso que usa ese puerto.",
                        e
                );
            }
            throw e;
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private boolean causedByBindException(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof BindException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}

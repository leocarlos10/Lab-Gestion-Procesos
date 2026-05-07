package com.solab.appdesktop.api;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.BindException;
import java.net.URI;

public class ApiHttpServer {

    private final CatalogoApiService catalogoApiService = new CatalogoApiService();
    private final SimuladorRuntimeService simuladorRuntimeService = new SimuladorRuntimeService(catalogoApiService);
    private com.sun.net.httpserver.HttpServer server;

    public void start() throws Exception {
        if (server != null) {
            return;
        }

        String host = System.getProperty("app.api.host", AppIntegrationConstants.APP1_API_HOST);
        int port = Integer.getInteger("app.api.port", AppIntegrationConstants.APP1_API_PORT);
        URI baseUri = URI.create("http://" + host + ":" + port + "/");

        try {
            ResourceConfig config = new ResourceConfig()
                    .register(new AbstractBinder() {
                        @Override
                        protected void configure() {
                            bind(catalogoApiService).to(CatalogoApiService.class);
                            bind(simuladorRuntimeService).to(SimuladorRuntimeService.class);
                        }
                    })
                    .register(CatalogoResource.class)
                    .register(SimuladorResource.class)
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

package com.solab.appdesktop.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/simulador")
@Produces(MediaType.APPLICATION_XML)
public class SimuladorResource {

    private final SimuladorRuntimeService runtimeService;

    @Inject
    public SimuladorResource(SimuladorRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @POST
    @Path("/iniciar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response iniciar(String body) {
        try {
            int catalogoId = parseIntJson(body, "catalogoId", 0);
            int quantum = parseIntJson(body, "quantum", 3);
            int th = parseIntJson(body, "th", 500);
            if (catalogoId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_XML)
                        .entity(XmlResponseBuilder.error("INVALID_REQUEST", "catalogoId es obligatorio"))
                        .build();
            }
            String estado = runtimeService.iniciar(catalogoId, th, quantum);
            return Response.ok(XmlResponseBuilder.respuestaEstado(estado), MediaType.APPLICATION_XML).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_XML)
                    .entity(XmlResponseBuilder.error("INVALID_REQUEST", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_XML)
                    .entity(XmlResponseBuilder.error("SIM_ERROR", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/pausar")
    public Response pausar() {
        String estado = runtimeService.pausar();
        return Response.ok(XmlResponseBuilder.respuestaEstado(estado), MediaType.APPLICATION_XML).build();
    }

    @POST
    @Path("/reanudar")
    public Response reanudar() {
        String estado = runtimeService.reanudar();
        return Response.ok(XmlResponseBuilder.respuestaEstado(estado), MediaType.APPLICATION_XML).build();
    }

    @POST
    @Path("/reiniciar")
    public Response reiniciar() {
        String estado = runtimeService.reiniciar();
        return Response.ok(XmlResponseBuilder.respuestaEstado(estado), MediaType.APPLICATION_XML).build();
    }

    @GET
    @Path("/{catalogoId}/estado")
    public Response estado(@PathParam("catalogoId") int catalogoId) {
        SimuladorRuntimeService.SimulacionSnapshot snapshot = runtimeService.estado(catalogoId);
        String xml = XmlResponseBuilder.simulacionEstado(snapshot.listos(), snapshot.ejecucion(), snapshot.espera(), snapshot.terminados());
        return Response.ok(xml, MediaType.APPLICATION_XML).build();
    }

    @OPTIONS
    @Path("/iniciar")
    public Response optionsIniciar() {
        return Response.ok().build();
    }

    @OPTIONS
    @Path("/pausar")
    public Response optionsPausar() {
        return Response.ok().build();
    }

    @OPTIONS
    @Path("/reanudar")
    public Response optionsReanudar() {
        return Response.ok().build();
    }

    @OPTIONS
    @Path("/reiniciar")
    public Response optionsReiniciar() {
        return Response.ok().build();
    }

    @OPTIONS
    @Path("/{catalogoId}/estado")
    public Response optionsEstado() {
        return Response.ok().build();
    }

    private int parseIntJson(String json, String field, int defaultValue) {
        if (json == null || json.isBlank()) {
            return defaultValue;
        }
        String regex = "\""+field+"\"\\s*:\\s*(\\d+)";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(json);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return defaultValue;
    }
}


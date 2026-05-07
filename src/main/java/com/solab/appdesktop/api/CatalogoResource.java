package com.solab.appdesktop.api;

import com.solab.appdesktop.dto.CatalogoConProcesos;
import com.solab.appdesktop.model.Proceso;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/catalogos")
@Produces(MediaType.APPLICATION_XML)
public class CatalogoResource {

    private final CatalogoApiService catalogoApiService;

    @Inject
    public CatalogoResource(CatalogoApiService catalogoApiService) {
        this.catalogoApiService = catalogoApiService;
    }

    @GET
    public Response obtenerCatalogos() {
        String xml = XmlResponseBuilder.catalogos(catalogoApiService.obtenerCatalogos());
        return Response.ok(xml, MediaType.APPLICATION_XML).build();
    }

    @OPTIONS
    public Response optionsCatalogos() {
        return Response.ok().build();
    }

    @GET
    @Path("/{id}")
    public Response obtenerCatalogo(@PathParam("id") int id) {
        return catalogoApiService.obtenerCatalogoPorId(id)
                .map(this::ok)
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_XML)
                        .entity(XmlResponseBuilder.error("CATALOGO_NOT_FOUND", "No existe un catalogo con id " + id + "."))
                        .build());
    }

    @OPTIONS
    @Path("/{id}")
    public Response optionsCatalogo() {
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/procesos")
    public Response obtenerProcesosCatalogo(@PathParam("id") int id) {
        return catalogoApiService.obtenerCatalogoPorId(id)
                .map(c -> {
                    List<Proceso> lista = c.getProcesos();
                    String xml = XmlResponseBuilder.procesosSolo(lista);
                    return Response.ok(xml, MediaType.APPLICATION_XML).build();
                })
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_XML)
                        .entity(XmlResponseBuilder.error("CATALOGO_NOT_FOUND", "No existe un catalogo con id " + id + "."))
                        .build());
    }

    @OPTIONS
    @Path("/{id}/procesos")
    public Response optionsProcesosCatalogo() {
        return Response.ok().build();
    }

    private Response ok(CatalogoConProcesos catalogo) {
        String xml = XmlResponseBuilder.catalogoDetalle(catalogo);
        return Response.ok(xml, MediaType.APPLICATION_XML).build();
    }
}

package com.solab.appdesktop.api;

import com.solab.appdesktop.dto.CatalogoConProcesos;
import com.solab.appdesktop.model.Proceso;

import java.util.List;

public final class XmlResponseBuilder {

    private XmlResponseBuilder() {
    }

    public static String catalogos(List<CatalogoConProcesos> catalogos) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<catalogos>\n");

        for (CatalogoConProcesos catalogo : catalogos) {
            xml.append("    <catalogo>\n");
            xml.append("        <id>").append(catalogo.getId()).append("</id>\n");
            xml.append("        <numero>").append(catalogo.getNumero()).append("</numero>\n");
            xml.append("        <nombre>").append(escape(catalogo.getNombre())).append("</nombre>\n");
            xml.append("        <fecha>").append(escape(catalogo.getFecha())).append("</fecha>\n");
            xml.append("    </catalogo>\n");
        }

        xml.append("</catalogos>\n");
        return xml.toString();
    }

    public static String catalogoDetalle(CatalogoConProcesos catalogo) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<catalogo>\n");
        xml.append("    <id>").append(catalogo.getId()).append("</id>\n");
        xml.append("    <numero>").append(catalogo.getNumero()).append("</numero>\n");
        xml.append("    <nombre>").append(escape(catalogo.getNombre())).append("</nombre>\n");
        xml.append("    <fecha>").append(escape(catalogo.getFecha())).append("</fecha>\n");
        xml.append("    <procesos>\n");

        if (catalogo.getProcesos() != null) {
            for (Proceso proceso : catalogo.getProcesos()) {
                xml.append("        <proceso>\n");
                xml.append("            <id>").append(proceso.getId()).append("</id>\n");
                xml.append("            <pid>").append(proceso.getPid()).append("</pid>\n");
                xml.append("            <nombre>").append(escape(proceso.getNombre())).append("</nombre>\n");
                xml.append("            <usuario>").append(escape(proceso.getUsuario())).append("</usuario>\n");
                xml.append("            <descripcion>").append(escape(proceso.getDescripcion())).append("</descripcion>\n");
                xml.append("            <prioridad>").append(proceso.getPrioridad()).append("</prioridad>\n");
                xml.append("        </proceso>\n");
            }
        }

        xml.append("    </procesos>\n");
        xml.append("</catalogo>\n");
        return xml.toString();
    }

    public static String error(String code, String message) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <error>
                    <code>%s</code>
                    <message>%s</message>
                </error>
                """.formatted(escape(code), escape(message));
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}

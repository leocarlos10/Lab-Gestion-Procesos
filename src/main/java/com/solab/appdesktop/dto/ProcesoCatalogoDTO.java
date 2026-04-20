package com.solab.appdesktop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcesoCatalogoDTO {
    private int pid;
    private String nombre;
    private String usuario;
    private String descripcion;
    private int prioridad;
    private long numeroCatalogo;
    private String nombreCatalogo;
}

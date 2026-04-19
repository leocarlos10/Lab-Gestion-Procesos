package com.solab.appdesktop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Proceso {

    private int id;
    private int catalogoId;
    private int pid;
    private String nombre;
    private String usuario;
    private String descripcion;
    private int prioridad; // 0 = Expulsivo, 1 = No Expulsivo
}

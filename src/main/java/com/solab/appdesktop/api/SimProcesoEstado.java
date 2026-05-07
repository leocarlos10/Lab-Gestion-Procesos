package com.solab.appdesktop.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimProcesoEstado {
    private int pid;
    private String nombre;
    private String estado;
    private int quantumTotal;
    private int caracteresEscritos;
    private int ejecuciones;
    private int tiempoLlegada;
    private int tiempoFinalizacion;
    private int turnaround;
    private int prioridad;
}


package com.solab.appdesktop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Catalogo {
    private Long id;
    private Long numero;
    private String nombre;
    private String fecha;
}

package com.solab.appdesktop.dto;

import com.solab.appdesktop.model.Proceso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CatalogoConProcesos {
    private int id;
    private int numero;
    private String nombre;
    private String fecha;
    private List<Proceso> procesos;
}

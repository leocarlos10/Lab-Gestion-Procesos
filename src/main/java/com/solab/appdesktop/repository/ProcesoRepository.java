package com.solab.appdesktop.repository;

import com.solab.appdesktop.model.Proceso;

import java.util.List;

public interface ProcesoRepository {

    void guardarProceso(List<Proceso> proceso, long catalogoId);
}

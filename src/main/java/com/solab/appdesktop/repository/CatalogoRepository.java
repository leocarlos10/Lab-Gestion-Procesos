package com.solab.appdesktop.repository;

import com.solab.appdesktop.dto.CatalogoConProcesos;
import com.solab.appdesktop.model.Catalogo;
import java.util.List;

public interface CatalogoRepository {

    Long guardarCatalogo(Catalogo catalogo);
    List<CatalogoConProcesos> obtenerCatalogosConProcesos();
}

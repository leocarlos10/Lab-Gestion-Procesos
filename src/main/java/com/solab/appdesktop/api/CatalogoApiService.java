package com.solab.appdesktop.api;

import com.solab.appdesktop.dto.CatalogoConProcesos;
import com.solab.appdesktop.repository.impl.CatalogoRepositoryImpl;

import java.util.List;
import java.util.Optional;

public class CatalogoApiService {

    private final CatalogoRepositoryImpl catalogoRepository = new CatalogoRepositoryImpl();

    public List<CatalogoConProcesos> obtenerCatalogos() {
        return catalogoRepository.obtenerCatalogosConProcesos();
    }

    public Optional<CatalogoConProcesos> obtenerCatalogoPorId(int id) {
        return obtenerCatalogos().stream()
                .filter(catalogo -> catalogo.getId() == id)
                .findFirst();
    }
}

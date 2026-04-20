package com.solab.appdesktop.service;

import com.solab.appdesktop.dto.CatalogoConProcesos;
import com.solab.appdesktop.model.Catalogo;
import com.solab.appdesktop.model.Proceso;
import com.solab.appdesktop.repository.CatalogoRepository;
import com.solab.appdesktop.repository.ProcesoRepository;
import com.solab.appdesktop.repository.impl.CatalogoRepositoryImpl;
import com.solab.appdesktop.repository.impl.ProcesoRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class CatalogoService {

    private ProcesoService procesoService;
    private CatalogoRepository catalogoRepository;
    private ProcesoRepository procesoRepository;

    // array para el manejo de catalgoso con procesos
    private List<CatalogoConProcesos> catalogoConProcesos = new ArrayList<>();

    public CatalogoService(
            ProcesoService procesoService,
            CatalogoRepositoryImpl catalogoRepositoryImpl,
            ProcesoRepositoryImpl procesoRepositoryImpl
    ) {
        this.procesoService = procesoService;
        this.catalogoRepository = catalogoRepositoryImpl;
        this.procesoRepository = procesoRepositoryImpl;
    }

    /**
     * servicio que guarda un catalogo con sus procesos
     * @param catalogo
     * @return
     */
    public void guardarCatalogoConProcesos(Catalogo catalogo){
        // primero guardamos el catalogo en la base de datos
        long catalogId =  catalogoRepository.guardarCatalogo(catalogo);
        // luego usamos el id para guardar los procesos asociados a ese catalogo
        List<Proceso> procesoList = procesoService.getProcesosCapturados();
        procesoRepository.guardarProceso(procesoList, catalogId);
    }

    public List<CatalogoConProcesos> getCatalogoConProcesos(){
        return catalogoConProcesos;
    }

    public void cargarCatalogoConProcesos(){
        this.catalogoConProcesos = catalogoRepository.obtenerCatalogosConProcesos();
    }
}

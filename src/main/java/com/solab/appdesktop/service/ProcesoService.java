package com.solab.appdesktop.service;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import com.solab.appdesktop.model.Proceso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProcesoService {

    private List<Proceso> procesosCapturados = new ArrayList<>();

    /**
     * Metodo get para obtener la lista de ProcesosCapturados
     * @return procesosCapturados: lista de procesos capturados
     */
    public List<Proceso> getProcesosCapturados() {
        return procesosCapturados;
    }

    /**
     * Este metodo es el encargado de obtener los procesos del OS
     * hacemos uso de la libreria OSHI para tener compatibilidad con diferentes sistemas operativos
     * @param cantidad cantidad de procesos a capturar
     * @param criterio criterio de ordenamiento (CPU o Memoria)
     * @return lista de procesos capturados
     */
    public List<Proceso> capturarProcesos(int cantidad, String criterio) {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();

        // Seleccionar criterio de ordenamiento
        Comparator<OSProcess> sort = criterio.equals("CPU")
                ? Comparator.comparingDouble(OSProcess::getProcessCpuLoadCumulative).reversed()
                : Comparator.comparingLong(OSProcess::getResidentSetSize).reversed();

        List<OSProcess> procesos = os.getProcesses(null, sort, cantidad);

        List<Proceso> resultado = new ArrayList<>();
        for (OSProcess p : procesos) {
            Proceso proceso = new Proceso();
            proceso.setPid(p.getProcessID());
            proceso.setNombre(p.getName());
            proceso.setUsuario(p.getUser());
            proceso.setDescripcion(p.getName());

            // Asignar prioridad según origen del proceso
            // Usuarios de sistema: root (Linux/Mac), SYSTEM (Windows)
            String usuario = p.getUser() != null ? p.getUser().toLowerCase() : "";
            boolean esSistema = usuario.equals("root") || usuario.equals("system")
                    || usuario.equals("daemon") || usuario.isEmpty();
            proceso.setPrioridad(esSistema ? 1 : 0);

            resultado.add(proceso);
        }
        // antes de retornar la lista guardamos una imagen de esta en procesosCapturados
        procesosCapturados = resultado;
        return resultado;
    }


}

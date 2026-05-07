package com.solab.appdesktop.service;

import com.solab.appdesktop.model.Proceso;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Captura procesos reales del SO usando {@link java.lang.ProcessHandle}.
 * El orden por CPU o memoria se obtiene vía {@link ProcessPidRanker} (PowerShell / ps).
 */
public class ProcesoService {

    private static final Set<String> USUARIOS_SISTEMA = Set.of(
            "root",
            "daemon",
            "bin",
            "sys",
            "system",
            "unknown",
            "n/a",
            "local service",
            "network service",
            "nt authority\\system",
            "nt authority\\local service",
            "nt authority\\network service",
            "_windowserver",
            "_taskgated",
            "_securityd",
            "_mdnsresponder",
            "_spotlight"
    );

    private List<Proceso> procesosCapturados = new ArrayList<>();

    public List<Proceso> getProcesosCapturados() {
        return procesosCapturados;
    }

    /**
     * Actualiza la descripción de un proceso capturado (quantum = longitud de descripción).
     */
    public void actualizarDescripcion(int pid, String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return;
        }
        for (Proceso p : procesosCapturados) {
            if (p.getPid() == pid) {
                p.setDescripcion(descripcion);
                return;
            }
        }
    }

    /**
     * @param cantidad cantidad de procesos a capturar
     * @param criterio   "CPU" o "Memoria"
     */
    public List<Proceso> capturarProcesos(int cantidad, String criterio) {
        int n = Math.max(1, cantidad);
        List<Proceso> resultado = new ArrayList<>();
        try {
            List<Long> ranked = ProcessPidRanker.topPids(n, criterio);
            for (Long pid : ranked) {
                Optional<ProcessHandle> opt = ProcessHandle.of(pid);
                if (opt.isEmpty() || !opt.get().isAlive()) {
                    continue;
                }
                Proceso p = mapHandleToProceso(opt.get());
                if (p != null) {
                    
                    if (p.getNombre() != null && 
                        !p.getNombre().isEmpty() &&
                        resultado.stream().noneMatch(existing -> existing.getNombre().equals(p.getNombre()))) {
                                
                        resultado.add(p);
                    }

                }
                if (resultado.size() >= n) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error ranking procesos: " + e.getMessage());
            // Respaldo: enumerar vivos y ordenar por tiempo de inicio (determinístico)
            resultado = fallbackTopAlive(n);
        }

        if (resultado.size() < n) {
            List<Proceso> more = fallbackTopAlive(n - resultado.size());
            for (Proceso p : more) {
                if (resultado.stream().noneMatch(x -> x.getPid() == p.getPid())) {
                   // System.out.println(resultado);
                    resultado.add(p);
                }
                if (resultado.size() >= n) {
                    break;
                }
            }
        }

        procesosCapturados = new ArrayList<>(resultado);
        return resultado;
    }

    private List<Proceso> fallbackTopAlive(int n) {
        return ProcessHandle.allProcesses()
                .filter(ProcessHandle::isAlive)
                .sorted(Comparator.comparingLong(ProcessHandle::pid))
                .limit(n)
                .map(this::mapHandleToProceso)
                .filter(p -> p != null)
                .toList();
    }

    private Proceso mapHandleToProceso(ProcessHandle handle) {
        ProcessHandle.Info info = handle.info();
        long pid = handle.pid();

        String command = info.command().orElse("").trim();
        String commandLine = info.commandLine().orElse("").trim();

        String nombre;
        if (!command.isEmpty()) {
            Path cmdPath = Paths.get(command);
            nombre = cmdPath.getFileName() != null ? cmdPath.getFileName().toString() : command;
        } else {
            nombre = "pid-" + pid;
        }

        // Descripción: línea de comando completa, o ruta del ejecutable, o nombre (nunca vacía)
        String descripcion = !commandLine.isEmpty() ? commandLine : (!command.isEmpty() ? command : nombre);
        if (descripcion == null || descripcion.isBlank()) {
            descripcion = nombre;
        }

        String usuarioRaw = info.user().orElse("").trim();
        String usuario = normalizeUsuarioSo(usuarioRaw);

        boolean esSistema = esUsuarioSistema(usuarioRaw.toLowerCase(Locale.ROOT), usuario);
        int prioridad = esSistema ? 1 : 0;

        return Proceso.builder()
                .pid((int) pid)
                .nombre(nombre)
                .usuario(usuario)
                .descripcion(descripcion)
                .prioridad(prioridad)
                .build();
    }

    /**
     * Muestra SYSTEM para cuentas del SO en Windows/Linux.
     */
    private static String normalizeUsuarioSo(String usuarioRaw) {
        if (usuarioRaw == null || usuarioRaw.isBlank()) {
            return "SYSTEM";
        }
        String u = usuarioRaw.trim();
        String lower = u.toLowerCase(Locale.ROOT);
        if (lower.contains("nt authority\\system") || "system".equals(lower)) {
            return "SYSTEM";
        }
        if ("0".equals(lower) || "root".equals(lower)) {
            return "SYSTEM";
        }
        return u;
    }

    private boolean esUsuarioSistema(String usuarioLower, String displayUser) {
        if (displayUser.equalsIgnoreCase("SYSTEM")) {
            return true;
        }
        if (usuarioLower == null || usuarioLower.isBlank()) {
            return true;
        }
        if (USUARIOS_SISTEMA.contains(usuarioLower)) {
            return true;
        }
        return usuarioLower.startsWith("nt authority\\")
                || usuarioLower.startsWith("nt service\\");
    }
}

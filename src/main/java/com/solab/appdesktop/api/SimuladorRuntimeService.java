package com.solab.appdesktop.api;

import com.solab.appdesktop.dto.CatalogoConProcesos;
import com.solab.appdesktop.model.Proceso;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Motor Round Robin in-memory para la API XML.
 * Cada proceso tiene su propio hilo y espera turno con monitor compartido.
 */
public class SimuladorRuntimeService {

    private final CatalogoApiService catalogoApiService;
    private final Object monitor = new Object();

    private final Map<Integer, RuntimeProceso> runtimeProcesos = new HashMap<>();
    private final ArrayDeque<Integer> colaListos = new ArrayDeque<>();

    private int catalogoIdActual = -1;
    private int quantum = 3;
    private long thMs = 500L;

    private boolean iniciado = false;
    /** Bandera global compartida por todos los hilos de proceso. */
    private volatile boolean simulacionPausada = false;
    private Integer pidEnCpu = null;

    public SimuladorRuntimeService(CatalogoApiService catalogoApiService) {
        this.catalogoApiService = catalogoApiService;
    }

    public String iniciar(int catalogoId, long th, int quantumNuevo) {
        synchronized (monitor) {
            if (iniciado && this.catalogoIdActual == catalogoId && !runtimeProcesos.isEmpty()) {
                this.thMs = Math.max(1L, th);
                this.quantum = Math.max(1, quantumNuevo);
                this.simulacionPausada = false;
                if (pidEnCpu == null) {
                    seleccionarSiguienteEnCpu();
                }
                monitor.notifyAll();
                return "iniciado";
            }
            reiniciarInterno(false);

            CatalogoConProcesos catalogo = catalogoApiService.obtenerCatalogoPorId(catalogoId)
                    .orElseThrow(() -> new IllegalArgumentException("Catalogo no encontrado: " + catalogoId));

            this.catalogoIdActual = catalogoId;
            this.thMs = Math.max(1L, th);
            this.quantum = Math.max(1, quantumNuevo);
            this.iniciado = true;
            this.simulacionPausada = false;
            this.pidEnCpu = null;

            List<Proceso> procesos = catalogo.getProcesos() == null ? List.of() : catalogo.getProcesos();
            for (int i = 0; i < procesos.size(); i++) {
                Proceso p = procesos.get(i);
                RuntimeProceso rp = new RuntimeProceso(p, i);
                runtimeProcesos.put(p.getPid(), rp);
                colaListos.addLast(p.getPid());
            }
            seleccionarSiguienteEnCpu();

            for (RuntimeProceso rp : runtimeProcesos.values()) {
                Thread t = new Thread(() -> loopProceso(rp), "SIM-" + rp.pid);
                rp.thread = t;
                t.start();
            }
            monitor.notifyAll();
        }
        return "iniciado";
    }

    public String pausar() {
        synchronized (monitor) {
            if (!iniciado) {
                return "pausado";
            }
            simulacionPausada = true;
            return "pausado";
        }
    }

    public String reanudar() {
        synchronized (monitor) {
            simulacionPausada = false;
            monitor.notifyAll();
            return "reanudado";
        }
    }

    public String reiniciar() {
        synchronized (monitor) {
            reiniciarInterno(true);
            return "reiniciado";
        }
    }

    public SimulacionSnapshot estado(int catalogoId) {
        synchronized (monitor) {
            if (!iniciado || this.catalogoIdActual != catalogoId) {
                return new SimulacionSnapshot(List.of(), List.of(), List.of(), List.of());
            }
            List<SimProcesoEstado> listos = new ArrayList<>();
            List<SimProcesoEstado> ejecucion = new ArrayList<>();
            List<SimProcesoEstado> espera = new ArrayList<>();
            List<SimProcesoEstado> terminados = new ArrayList<>();

            for (RuntimeProceso rp : runtimeProcesos.values()) {
                SimProcesoEstado dto = rp.toDto();
                switch (rp.estado) {
                    case "listo" -> listos.add(dto);
                    case "ejecucion" -> ejecucion.add(dto);
                    case "espera" -> espera.add(dto);
                    case "terminado" -> terminados.add(dto);
                    default -> listos.add(dto);
                }
            }

            Comparator<SimProcesoEstado> byLlegada = Comparator.comparingInt(SimProcesoEstado::getTiempoLlegada);
            listos.sort(byLlegada);
            espera.sort(byLlegada);
            terminados.sort(byLlegada);

            return new SimulacionSnapshot(listos, ejecucion, espera, terminados);
        }
    }

    private void loopProceso(RuntimeProceso rp) {
        try {
            crearArchivoSiNoExiste(rp);
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (monitor) {
                    while (iniciado && (!esTurno(rp.pid) || "terminado".equals(rp.estado))) {
                        monitor.wait();
                    }
                    if (!iniciado || "terminado".equals(rp.estado)) {
                        return;
                    }
                    rp.estado = "ejecucion";
                }

                int porEscribir;
                synchronized (monitor) {
                    int restantes = rp.quantumTotal - rp.caracteresEscritos;
                    if (restantes <= 0) {
                        marcarTerminado(rp);
                        continue;
                    }
                    porEscribir = rp.prioridad == 1 ? restantes : Math.min(quantum, restantes);
                }

                for (int i = 0; i < porEscribir; i++) {
                    while (iniciado && simulacionPausada && !Thread.currentThread().isInterrupted()) {
                        Thread.sleep(100);
                    }
                    char ch;
                    synchronized (monitor) {
                        if (!iniciado || !esTurno(rp.pid)) {
                            break;
                        }
                        int idx = rp.caracteresEscritos;
                        if (idx >= rp.quantumTotal) {
                            break;
                        }
                        ch = rp.descripcion.charAt(idx);
                    }

                    escribirCaracter(rp, ch);

                    synchronized (monitor) {
                        rp.caracteresEscritos++;
                        rp.ejecuciones++;
                        if (rp.caracteresEscritos >= rp.quantumTotal) {
                            marcarTerminado(rp);
                            break;
                        }
                    }

                    Thread.sleep(thMs);
                }

                synchronized (monitor) {
                    if ("terminado".equals(rp.estado) || !iniciado) {
                        monitor.notifyAll();
                        continue;
                    }
                    if (rp.prioridad == 0) {
                        rp.estado = "espera";
                        colaListos.addLast(rp.pid);
                        pidEnCpu = null;
                        // Estado breve de espera antes de volver a listo.
                        rp.estado = "listo";
                        seleccionarSiguienteEnCpu();
                    } else {
                        pidEnCpu = rp.pid;
                    }
                    monitor.notifyAll();
                }
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            synchronized (monitor) {
                rp.estado = "terminado";
                pidEnCpu = null;
                seleccionarSiguienteEnCpu();
                monitor.notifyAll();
            }
        }
    }

    private void seleccionarSiguienteEnCpu() {
        if (pidEnCpu != null) {
            RuntimeProceso running = runtimeProcesos.get(pidEnCpu);
            if (running != null && !"terminado".equals(running.estado)) {
                return;
            }
            pidEnCpu = null;
        }
        while (!colaListos.isEmpty()) {
            int pid = colaListos.removeFirst();
            RuntimeProceso rp = runtimeProcesos.get(pid);
            if (rp != null && !"terminado".equals(rp.estado)) {
                pidEnCpu = pid;
                rp.estado = "listo";
                return;
            }
        }
    }

    private boolean esTurno(int pid) {
        return pidEnCpu != null && pidEnCpu == pid;
    }

    private void marcarTerminado(RuntimeProceso rp) {
        rp.estado = "terminado";
        rp.tiempoFinalizacion = rp.quantumTotal * rp.ejecuciones;
        rp.turnaround = rp.tiempoFinalizacion - rp.tiempoLlegada;
        if (pidEnCpu != null && pidEnCpu == rp.pid) {
            pidEnCpu = null;
        }
        seleccionarSiguienteEnCpu();
    }

    private void reiniciarInterno(boolean limpiarArchivos) {
        iniciado = false;
        simulacionPausada = false;
        pidEnCpu = null;
        for (RuntimeProceso rp : runtimeProcesos.values()) {
            if (rp.thread != null) {
                rp.thread.interrupt();
            }
            rp.estado = "listo";
            rp.caracteresEscritos = 0;
            rp.ejecuciones = 0;
            rp.tiempoFinalizacion = 0;
            rp.turnaround = 0;
        }
        runtimeProcesos.clear();
        colaListos.clear();
        monitor.notifyAll();

        if (limpiarArchivos && catalogoIdActual > 0) {
            Path dir = Paths.get("Actividades", "catalogo_" + catalogoIdActual);
            if (Files.exists(dir)) {
                try {
                    Files.walk(dir)
                            .sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException ignored) {
                                }
                            });
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void crearArchivoSiNoExiste(RuntimeProceso rp) throws IOException {
        Path dir = Paths.get("Actividades", "catalogo_" + catalogoIdActual);
        Files.createDirectories(dir);
        Path file = dir.resolve(rp.fileName);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
    }

    private void escribirCaracter(RuntimeProceso rp, char ch) throws IOException {
        Path dir = Paths.get("Actividades", "catalogo_" + catalogoIdActual);
        Path file = dir.resolve(rp.fileName);
        Files.writeString(file, String.valueOf(ch), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    public record SimulacionSnapshot(
            List<SimProcesoEstado> listos,
            List<SimProcesoEstado> ejecucion,
            List<SimProcesoEstado> espera,
            List<SimProcesoEstado> terminados
    ) {
    }

    private static class RuntimeProceso {
        final int pid;
        final String nombre;
        final String usuario;
        final String descripcion;
        final int prioridad;
        final int quantumTotal;
        final int tiempoLlegada;
        final String fileName;

        volatile String estado = "listo";
        volatile int caracteresEscritos = 0;
        volatile int ejecuciones = 0;
        volatile int tiempoFinalizacion = 0;
        volatile int turnaround = 0;
        volatile Thread thread;

        RuntimeProceso(Proceso p, int indexLlegada) {
            this.pid = p.getPid();
            this.nombre = p.getNombre() == null ? "proceso-" + p.getPid() : p.getNombre();
            this.usuario = p.getUsuario() == null ? "" : p.getUsuario();
            String desc = p.getDescripcion();
            if (desc == null || desc.isBlank()) {
                desc = this.nombre;
            }
            this.descripcion = desc;
            this.prioridad = p.getPrioridad();
            this.quantumTotal = desc.length();
            this.tiempoLlegada = indexLlegada;
            this.fileName = safeFileName(this.nombre) + ".txt";
        }

        SimProcesoEstado toDto() {
            return SimProcesoEstado.builder()
                    .pid(pid)
                    .nombre(nombre)
                    .estado(estado)
                    .quantumTotal(quantumTotal)
                    .caracteresEscritos(caracteresEscritos)
                    .ejecuciones(ejecuciones)
                    .tiempoLlegada(tiempoLlegada)
                    .tiempoFinalizacion(tiempoFinalizacion)
                    .turnaround(turnaround)
                    .prioridad(prioridad)
                    .build();
        }

        private static String safeFileName(String input) {
            String s = input == null ? "proceso" : input.replaceAll("[^a-zA-Z0-9._-]", "_");
            if (s.isBlank()) {
                return "proceso";
            }
            return s;
        }
    }
}


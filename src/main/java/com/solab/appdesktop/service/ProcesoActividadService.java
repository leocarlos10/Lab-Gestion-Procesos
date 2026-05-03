package com.solab.appdesktop.service;

import com.solab.appdesktop.model.Proceso;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ProcesoActividadService {

    private static final long TH_MS = 500L;
    private final Path baseDir;

    public ProcesoActividadService() {
        this(Paths.get("Actividades"));
    }

    public ProcesoActividadService(Path baseDir) {
        this.baseDir = baseDir;
    }

    public List<Thread> ejecutarActividad(List<Proceso> procesos) {
        return ejecutarActividad(procesos, TH_MS);
    }

    List<Thread> ejecutarActividad(List<Proceso> procesos, long thMs) {
        List<Thread> threads = new ArrayList<>();
        if (procesos == null || procesos.isEmpty()) {
            return threads;
        }

        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            System.err.println("No se pudo crear la carpeta de actividades: " + baseDir);
            return threads;
        }

        for (Proceso proceso : procesos) {
            Thread thread = new Thread(() -> ejecutarActividadProceso(proceso, thMs));
            thread.setName("RF05-" + proceso.getPid());
            threads.add(thread);
            thread.start();
        }

        return threads;
    }

    private void ejecutarActividadProceso(Proceso proceso, long thMs) {
        String descripcion = proceso.getDescripcion() != null ? proceso.getDescripcion() : "";
        Path filePath = baseDir.resolve(buildFileName(proceso));

        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            for (char c : descripcion.toCharArray()) {
                writer.write(c);
                writer.flush();
                Thread.sleep(thMs);
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo archivo de proceso: " + filePath + " - " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String buildFileName(Proceso proceso) {
        String nombre = proceso.getNombre() != null ? proceso.getNombre() : "proceso";
        String safe = nombre.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safe.isBlank()) {
            safe = "proceso";
        }
        return safe + "_" + proceso.getPid() + ".txt";
    }
}


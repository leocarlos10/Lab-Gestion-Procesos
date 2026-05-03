package com.solab.appdesktop.service;

import com.solab.appdesktop.model.Proceso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcesoActividadServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void escribeArchivoConDescripcion() throws Exception {
        Proceso proceso = Proceso.builder()
                .pid(123)
                .nombre("demo")
                .descripcion("ab")
                .build();

        ProcesoActividadService service = new ProcesoActividadService(tempDir);
        List<Thread> threads = service.ejecutarActividad(List.of(proceso));
        for (Thread thread : threads) {
            thread.join(5000);
        }

        Path expected = tempDir.resolve("demo_123.txt");
        assertTrue(Files.exists(expected));
        assertEquals("ab", Files.readString(expected));
    }
}


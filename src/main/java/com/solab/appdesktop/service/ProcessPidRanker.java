package com.solab.appdesktop.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Obtiene los PIDs ordenados por CPU o memoria usando comandos del SO.
 * Los detalles del proceso se resuelven después con {@link java.lang.ProcessHandle}.
 */
final class ProcessPidRanker {

    private ProcessPidRanker() {
    }

    static List<Long> topPids(int cantidad, String criterio) throws IOException, InterruptedException {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return topPidsWindows(cantidad, criterio);
        }
        return topPidsUnix(cantidad, criterio);
    }

    private static List<Long> topPidsWindows(int cantidad, String criterio) throws IOException, InterruptedException {
        boolean byCpu = "CPU".equalsIgnoreCase(criterio);
        String script = byCpu
                ? String.format(
                "Get-Process | Sort-Object CPU -Descending | Select-Object -First %d -ExpandProperty Id",
                cantidad)
                : String.format(
                "Get-Process | Sort-Object WorkingSet64 -Descending | Select-Object -First %d -ExpandProperty Id",
                cantidad);
        ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-NonInteractive",
                "-Command",
                script
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            List<Long> out = r.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(ProcessPidRanker::parseLongSafe)
                    .filter(v -> v > 0)
                    .collect(Collectors.toCollection(ArrayList::new));
            int code = p.waitFor();
            if (code != 0 && out.isEmpty()) {
                throw new IOException("PowerShell ranking exit code " + code);
            }
            return out;
        }
    }

    private static List<Long> topPidsUnix(int cantidad, String criterio) throws IOException, InterruptedException {
        boolean byCpu = "CPU".equalsIgnoreCase(criterio);
        // ps: orden por %mem o %cpu, sin encabezado, solo pid
        String key = byCpu ? "-%cpu" : "-%mem";
        ProcessBuilder pb = new ProcessBuilder(
                "sh",
                "-c",
                "ps -eo pid --no-headers --sort=" + key + " | head -n " + cantidad
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            List<Long> out = r.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(ProcessPidRanker::parseLongSafe)
                    .filter(v -> v > 0)
                    .collect(Collectors.toCollection(ArrayList::new));
            p.waitFor();
            return out;
        }
    }

    private static long parseLongSafe(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

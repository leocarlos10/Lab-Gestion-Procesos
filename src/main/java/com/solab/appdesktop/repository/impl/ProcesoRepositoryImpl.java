package com.solab.appdesktop.repository.impl;

import com.solab.appdesktop.model.Proceso;
import com.solab.appdesktop.repository.ProcesoRepository;
import com.solab.appdesktop.repository.SQLiteUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ProcesoRepositoryImpl implements ProcesoRepository {

    @Override
    public void guardarProceso(List<Proceso> procesos, long catalogoId) {
        if (procesos == null || procesos.isEmpty()) {
            System.out.println("No hay procesos para guardar.");
            return;
        }

        String sql = "INSERT INTO proceso (catalogo_id, pid, nombre, usuario, descripcion, prioridad) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = SQLiteUtil.getConnection();
            boolean autoCommitPrevio = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (Proceso proceso : procesos) {
                    stmt.setLong(1, catalogoId);
                    stmt.setInt(2, proceso.getPid());
                    stmt.setString(3, proceso.getNombre());
                    stmt.setString(4, proceso.getUsuario());
                    stmt.setString(5, proceso.getDescripcion());
                    stmt.setInt(6, proceso.getPrioridad());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommitPrevio);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando procesos en SQLite", e);
        }
    }
}

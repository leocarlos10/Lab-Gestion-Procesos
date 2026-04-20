package com.solab.appdesktop.repository.impl;

import com.solab.appdesktop.dto.CatalogoConProcesos;
import com.solab.appdesktop.model.Catalogo;
import com.solab.appdesktop.model.Proceso;
import com.solab.appdesktop.repository.CatalogoRepository;
import com.solab.appdesktop.repository.SQLiteUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CatalogoRepositoryImpl implements CatalogoRepository {

	@Override
	public Long guardarCatalogo(Catalogo catalogo) {
		String sql = "INSERT INTO catalogo (numero, nombre, fecha) VALUES (?, ?, ?)";

		try {
			Connection connection = SQLiteUtil.getConnection();
			try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
				stmt.setLong(1, catalogo.getNumero());
				stmt.setString(2, catalogo.getNombre());
				stmt.setString(3, catalogo.getFecha());
				stmt.executeUpdate();

				try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getLong(1);
					}
				}
			}
			throw new RuntimeException("No se pudo obtener el id generado del catalogo.");
		} catch (SQLException e) {
			throw new RuntimeException("Error guardando catalogo en SQLite", e);
		}
	}

	@Override
	public List<CatalogoConProcesos> obtenerCatalogosConProcesos() {
		String sql = "SELECT c.id AS c_id, c.numero AS c_numero, c.nombre AS c_nombre, c.fecha AS c_fecha, "
				+ "p.id AS p_id, p.pid AS p_pid, p.nombre AS p_nombre, p.usuario AS p_usuario, "
				+ "p.descripcion AS p_descripcion, p.prioridad AS p_prioridad "
				+ "FROM catalogo c "
				+ "LEFT JOIN proceso p ON c.id = p.catalogo_id "
				+ "ORDER BY c.id";

		List<CatalogoConProcesos> catalogos = new ArrayList<>();
		try {
			Connection connection = SQLiteUtil.getConnection();
			try (PreparedStatement stmt = connection.prepareStatement(sql);
				 ResultSet rs = stmt.executeQuery()) {

				int lastCatalogoId = -1;
				CatalogoConProcesos catalogo = null;
				List<Proceso> procesos = null;

				while (rs.next()) {
					int catalogoId = rs.getInt("c_id");
					// Si cambió el catálogo o es el primero, crear uno nuevo
					if (catalogo == null || catalogoId != lastCatalogoId) {
						catalogo = CatalogoConProcesos.builder()
								.id(catalogoId)
								.numero(rs.getInt("c_numero"))
								.nombre(rs.getString("c_nombre"))
								.fecha(rs.getString("c_fecha"))
								.procesos(new ArrayList<>())
								.build();
						catalogos.add(catalogo);
						procesos = catalogo.getProcesos();
						lastCatalogoId = catalogoId;
					}

					// Agregar el proceso si existe
					long procesoId = rs.getLong("p_id");
					if (!rs.wasNull()) {
						Proceso proceso = Proceso.builder()
								.id((int) procesoId)
								.pid(rs.getInt("p_pid"))
								.nombre(rs.getString("p_nombre"))
								.usuario(rs.getString("p_usuario"))
								.descripcion(rs.getString("p_descripcion"))
								.prioridad(rs.getInt("p_prioridad"))
								.build();
						procesos.add(proceso);
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error recuperando catalogos con procesos en SQLite", e);
		}
		return catalogos;
	}
}

package com.solab.appdesktop;

import com.solab.appdesktop.repository.SQLiteUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class GestionProcApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // ejecutamos la inicializacion de las tablas de la base de datos
        try {
            SQLiteUtil.crearTablas();
        }catch(SQLException e){
            e.printStackTrace();
        }
        FXMLLoader fxmlLoader = new FXMLLoader(GestionProcApplication.class.getResource("procesos-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Gestion De Procesos - App de Escritorio");
        stage.setScene(scene);
        stage.show();
    }
}

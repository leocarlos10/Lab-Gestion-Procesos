package com.solab.appdesktop;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import javax.swing.*;

public class GestionProcController {

    @FXML
    private Button buttonCapturarPro;

    @FXML
    private Button buttonGuardarCat;

    @FXML
    private ToggleGroup criterioGroup;

    @FXML
    private TextField nCatalogo;

    @FXML
    private TextField nProcesos;

    @FXML
    private TextField nomCatalogo;

    @FXML
    private TableView<?> tableProcesos;

    @FXML
    void eventCapturarPro(ActionEvent event) {

        JOptionPane.showMessageDialog(null, "evento capturar proceso ejecutado");

    }

    @FXML
    void eventGuardarCat(ActionEvent event) {
        JOptionPane.showMessageDialog(null, "evento guardar catalogo ejecutado");

    }

}

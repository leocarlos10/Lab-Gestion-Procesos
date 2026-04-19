package com.solab.appdesktop.controller;

import com.solab.appdesktop.model.Proceso;
import com.solab.appdesktop.service.ProcesoService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.util.List;


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
    private TableView<Proceso> tableProcesos;

    @FXML
    private TableColumn<Proceso, String> colDescripcion;

    @FXML
    private TableColumn<Proceso, String> colNombre;

    @FXML
    private TableColumn<Proceso, Integer> colPID;

    @FXML
    private TableColumn<Proceso, String> colPrioridad;

    @FXML
    private TableColumn<Proceso, String> colUsuario;

    private  ProcesoService procesoService = new ProcesoService();

    @FXML
    public void initialize(){
        /*
        * Configuracion de las columnas para cargar los procesos
        * capturados.
        *
        * relaciona una TableColumn -> atributo de la clase Proceso
        *
        * */
        colPID.setCellValueFactory(new PropertyValueFactory<>("pid"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrioridad.setCellValueFactory(cellData -> {
            int prioridad = cellData.getValue().getPrioridad();
            String texto = (prioridad == 0) ? "Expulsivo" : "No Expulsivo";
            return new ReadOnlyStringWrapper(texto);
        });
    }

    @FXML
    void eventCapturarPro(ActionEvent event) {

        // obtenemos los datos de los campos
        try {
            int nProc = Integer.parseInt( nProcesos.getText());

            RadioButton seleccionado = (RadioButton) criterioGroup.getSelectedToggle();
            String criterio = (seleccionado != null) ? seleccionado.getText() : "No seleccionado";

            // ejecutamos cargar Procesos
            cargarProcesos(nProc, criterio);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Por favor ingrese un número válido para la cantidad de procesos.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /**
     * Este metodo carga los procesos capturados en el tableview
     * @param cantidad cantidad de procesos a capturar
     * @param criterio criterio de ordenamiento (CPU o Memoria)
     */
    protected void cargarProcesos(int cantidad, String criterio) {
        // ejecutamos el metodo del service que obtiene los procesos
        List<Proceso> procesoList = procesoService.capturarProcesos(cantidad, criterio);
        //verifamos que la lista de procesos no este vacia
        if(!procesoList.isEmpty()){
            tableProcesos.getItems().setAll(procesoList);
        } else {
            tableProcesos.getItems().clear();
            System.out.println("La lista de procesos esta vacia.");
        }
    }

    @FXML
    void eventGuardarCat(ActionEvent event) {
        JOptionPane.showMessageDialog(null, "evento guardar catalogo ejecutado");

    }

}

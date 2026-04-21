package com.solab.appdesktop.controller;

import com.solab.appdesktop.dto.CatalogoConProcesos;
import com.solab.appdesktop.dto.ProcesoCatalogoDTO;
import com.solab.appdesktop.model.Catalogo;
import com.solab.appdesktop.model.Proceso;
import com.solab.appdesktop.repository.impl.CatalogoRepositoryImpl;
import com.solab.appdesktop.repository.impl.ProcesoRepositoryImpl;
import com.solab.appdesktop.service.CatalogoService;
import com.solab.appdesktop.service.ProcesoService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDate;

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
    private TableView<ProcesoCatalogoDTO> tableProcesos;

    @FXML
    private TableColumn<ProcesoCatalogoDTO, String> colDescripcion;

    @FXML
    private TableColumn<ProcesoCatalogoDTO, String> colNombre;

    @FXML
    private TableColumn<ProcesoCatalogoDTO, Integer> colPID;

    @FXML
    private TableColumn<ProcesoCatalogoDTO, String> colPrioridad;

    @FXML
    private TableColumn<ProcesoCatalogoDTO, String> colUsuario;

    @FXML
    private TableColumn<ProcesoCatalogoDTO, String> colNombreCat;

    @FXML
    private TableColumn<ProcesoCatalogoDTO, Long> colNumeroCat;

    @FXML
    private ComboBox<CatalogoConProcesos> comboCatalogos;

    private  ProcesoService procesoService = new ProcesoService();


    private CatalogoService catalogoService = new CatalogoService(
            this.procesoService,
            new CatalogoRepositoryImpl(),
            new ProcesoRepositoryImpl()
    );

    @FXML
    public void initialize(){
        /*
        * Configuracion de las columnas para cargar los procesos
        * capturados.
        *
        * relaciona una TableColumn -> atributo de la clase ProcesoCatalogoDTO
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
        colNumeroCat.setCellValueFactory(new PropertyValueFactory<>("numeroCatalogo"));
        colNombreCat.setCellValueFactory(new PropertyValueFactory<>("nombreCatalogo"));
        tableProcesos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableProcesos.setPlaceholder(new Label("No hay procesos o catalogos para mostrar."));

        configurarVisualizacionComboCatalogos();
        cargarCatalogosEnCombo();
        configurarEventoSeleccionCatalogo();
    }

    private void configurarVisualizacionComboCatalogos() {
        comboCatalogos.setConverter(new StringConverter<>() {
            @Override
            public String toString(CatalogoConProcesos catalogo) {
                if (catalogo == null) {
                    return "";
                }
                return catalogo.getNumero() + " - " + catalogo.getNombre();
            }

            @Override
            public CatalogoConProcesos fromString(String string) {
                return null;
            }
        });
    }

    private void cargarCatalogosEnCombo() {
        catalogoService.cargarCatalogoConProcesos();
        List<CatalogoConProcesos> catalogos = catalogoService.getCatalogoConProcesos();
        comboCatalogos.getItems().clear();
        if (catalogos != null && !catalogos.isEmpty()) {
            comboCatalogos.getItems().setAll(catalogos);
        }
    }

    private void configurarEventoSeleccionCatalogo() {
        comboCatalogos.valueProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado == null || seleccionado.getProcesos() == null || seleccionado.getProcesos().isEmpty()) {
                tableProcesos.getItems().clear();
                return;
            }

            List<ProcesoCatalogoDTO> procesosTabla = seleccionado.getProcesos().stream()
                    .map(proceso -> ProcesoCatalogoDTO.builder()
                            .pid(proceso.getPid())
                            .nombre(proceso.getNombre())
                            .usuario(proceso.getUsuario())
                            .descripcion(proceso.getDescripcion())
                            .prioridad(proceso.getPrioridad())
                            .numeroCatalogo(seleccionado.getNumero())
                            .nombreCatalogo(seleccionado.getNombre())
                            .build())
                    .toList();

            tableProcesos.getItems().setAll(procesosTabla);
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
            limpiarCampos(nProcesos);
            limpiarToggleGroup(criterioGroup);

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
    private void cargarProcesos(int cantidad, String criterio) {
        // ejecutamos el metodo del service que obtiene los procesos
        List<Proceso> procesoList = procesoService.capturarProcesos(cantidad, criterio);
        //verifamos que la lista de procesos no este vacia
        if(!procesoList.isEmpty()){
            // Convertir Proceso a ProcesoCatalogoDTO
            List<ProcesoCatalogoDTO> dtoList = procesoList.stream()
                    .map(p -> ProcesoCatalogoDTO.builder()
                            .pid(p.getPid())
                            .nombre(p.getNombre())
                            .usuario(p.getUsuario())
                            .descripcion(p.getDescripcion())
                            .prioridad(p.getPrioridad())
                            .numeroCatalogo(0) // Sin catálogo asignado aún
                            .nombreCatalogo("") // Sin catálogo asignado aún
                            .build())
                    .toList();
            tableProcesos.getItems().setAll(dtoList);
        } else {
            tableProcesos.getItems().clear();
            System.out.println("La lista de procesos esta vacia.");
        }
    }

    @FXML
    void eventGuardarCat(ActionEvent event) {
        // obtenemos los datos del catalogo
        try {

            int numCatalogo = Integer.parseInt(nCatalogo.getText());
            String nomCatalogo = this.nomCatalogo.getText();

            // ejecutamos el metodo de guardado de un catalogo
            guardarCatalogo(numCatalogo, nomCatalogo);

        }catch(Exception e){
            JOptionPane.showMessageDialog(
                    null,
                    "Por favor ingrese un numero valido para el catalogo ",
                    "Info",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Metodo generico para limpiar campos de texto en la interfaz.
     * @param campos campos que se desean limpiar
     */
    private void limpiarCampos(TextField... campos) {
        for (TextField campo : campos) {
            if (campo != null) {
                campo.clear();
            }
        }
    }

    /**
     * Metodo para limpiar la seleccion actual de un ToggleGroup.
     * @param grupo grupo de opciones que se desea limpiar
     */
    private void limpiarToggleGroup(ToggleGroup grupo) {
        if (grupo != null) {
            grupo.selectToggle(null);
        }
    }

    /**
     * Metodo que se encarga de preparar los datos necesarios
     * para guardar un catalogo.
     * @param numCat
     * @param nomCat
     */
    private void guardarCatalogo(int numCat, String nomCat){
        // creamos el catalogo
        Catalogo catalogo = Catalogo.builder()
                .numero(numCat)
                .nombre(nomCat)
                .fecha(LocalDate.now().toString())
                .build();

        // ejecutamos el guardado de los datos del catalago.
        catalogoService.guardarCatalogoConProcesos(catalogo);

        // Refresca el combo para incluir el nuevo catalogo guardado.
        cargarCatalogosEnCombo();
        limpiarCampos(nCatalogo, nomCatalogo);
        JOptionPane.showMessageDialog(null, "catalogo guardado correctamente", "INFO", JOptionPane.INFORMATION_MESSAGE);
    }

}

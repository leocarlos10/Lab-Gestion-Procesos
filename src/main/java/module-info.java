module com.solab.appdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    opens com.solab.appdesktop to javafx.fxml;
    exports com.solab.appdesktop;
}
module com.solab.appdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires jdk.httpserver;
    requires jakarta.ws.rs;
    requires jersey.common;
    requires jersey.server;
    requires jersey.hk2;
    requires static lombok;
    requires com.github.oshi;
    opens com.solab.appdesktop to javafx.fxml;
    exports com.solab.appdesktop;
    exports com.solab.appdesktop.controller;
    opens com.solab.appdesktop.controller to javafx.fxml;
    opens com.solab.appdesktop.model to javafx.base;
    exports com.solab.appdesktop.dto;
    opens com.solab.appdesktop.dto to javafx.fxml;
    exports com.solab.appdesktop.api;
    opens com.solab.appdesktop.api to jersey.server, jersey.hk2;
}

module com.mycompany.finalprojectclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires javafx.media;
    requires com.google.gson;

    opens com.mycompany.finalprojectclient to javafx.fxml, com.google.gson;
    opens com.mycompany.finalprojectclient.controllers to javafx.fxml;
    opens com.mycompany.finalprojectclient.models to com.google.gson;
    opens com.mycompany.finalprojectclient.utils to javafx.fxml;

    exports com.mycompany.finalprojectclient;
    exports com.mycompany.finalprojectclient.controllers;
    exports com.mycompany.finalprojectclient.models;
    exports com.mycompany.finalprojectclient.network;
    exports com.mycompany.finalprojectclient.utils;
}

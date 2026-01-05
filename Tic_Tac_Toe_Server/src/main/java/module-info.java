module com.mycompany.tic_tac_toe_server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;
    requires java.desktop;
    requires com.google.gson;
    
    opens com.mycompany.tic_tac_toe_server to javafx.fxml, com.google.gson;
    exports com.mycompany.tic_tac_toe_server;
}

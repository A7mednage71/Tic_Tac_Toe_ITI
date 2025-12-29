module com.mycompany.tic_tac_toe_server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens com.mycompany.tic_tac_toe_server to javafx.fxml;
    exports com.mycompany.tic_tac_toe_server;
}

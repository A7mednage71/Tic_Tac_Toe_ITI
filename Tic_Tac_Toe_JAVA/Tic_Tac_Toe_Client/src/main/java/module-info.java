module com.mycompany.finalprojectclient {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.finalprojectclient to javafx.fxml;
    exports com.mycompany.finalprojectclient;
}

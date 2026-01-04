module com.mycompany.finalprojectclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires javafx.media;

    opens com.mycompany.finalprojectclient to javafx.fxml;
    exports com.mycompany.finalprojectclient;
}

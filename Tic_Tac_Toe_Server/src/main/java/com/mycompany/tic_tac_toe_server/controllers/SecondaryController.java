package com.mycompany.tic_tac_toe_server.controllers;

import java.io.IOException;

import com.mycompany.tic_tac_toe_server.App;

import javafx.fxml.FXML;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}
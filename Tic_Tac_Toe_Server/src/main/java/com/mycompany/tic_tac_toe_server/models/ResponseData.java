package com.mycompany.tic_tac_toe_server.models;

public class ResponseData {

    public ResponseStatus status;   //  "SUCCESS" ,"FAILURE
    public String message;

    public ResponseData() {
    }

    public ResponseData(ResponseStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}

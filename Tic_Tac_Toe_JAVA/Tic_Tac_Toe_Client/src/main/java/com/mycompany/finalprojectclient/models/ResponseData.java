package com.mycompany.finalprojectclient.models;

public class ResponseData {

    public ResponseStatus status;   
    public String message;

    public ResponseData() {
    }

    public ResponseData(ResponseStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}

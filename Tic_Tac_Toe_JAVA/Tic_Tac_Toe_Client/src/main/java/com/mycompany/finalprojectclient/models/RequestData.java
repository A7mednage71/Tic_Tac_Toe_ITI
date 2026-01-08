package com.mycompany.finalprojectclient.models;

public class RequestData {

    public RequestType key;
    public String username;
    public String password;
    public String targetUsername;
    public String status;

    public RequestData() {
    }

    public RequestData(RequestType key, String username, String password) {
        this.key = key;
        this.username = username;
        this.password = password;
    }
}

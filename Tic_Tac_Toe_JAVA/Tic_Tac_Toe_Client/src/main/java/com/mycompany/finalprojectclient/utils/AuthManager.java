package com.mycompany.finalprojectclient.utils;

import java.io.IOException;

import com.google.gson.Gson;
import com.mycompany.finalprojectclient.models.RequestData;
import com.mycompany.finalprojectclient.models.RequestType;
import com.mycompany.finalprojectclient.models.ResponseData;
import com.mycompany.finalprojectclient.models.ResponseStatus;
import com.mycompany.finalprojectclient.network.ServerConnection;

public class AuthManager {

    private static AuthManager instance;
    private final Gson gson = new Gson();

    private AuthManager() {
    }

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public ResponseData login(String username, String password) {
        RequestData req = new RequestData(RequestType.LOGIN, username, password);
        try {
            String jsonResponse = ServerConnection.getInstance().sendRequest(req);
            return gson.fromJson(jsonResponse, ResponseData.class);
        } catch (IOException e) {
            return new ResponseData(ResponseStatus.FAILURE, "Connection Failed: Server unreachable");
        }
    }

    public ResponseData register(String username, String password) {
        RequestData req = new RequestData(RequestType.REGISTER, username, password);
        try {
            String jsonResponse = ServerConnection.getInstance().sendRequest(req);
            return gson.fromJson(jsonResponse, ResponseData.class);
        } catch (IOException e) {
            return new ResponseData(ResponseStatus.FAILURE, "Connection Failed: Server unreachable");
        }
    }
}

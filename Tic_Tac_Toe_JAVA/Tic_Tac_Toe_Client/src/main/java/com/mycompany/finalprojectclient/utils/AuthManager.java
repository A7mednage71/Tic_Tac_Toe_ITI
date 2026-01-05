package com.mycompany.finalprojectclient.utils;

import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.finalprojectclient.models.RequestData;
import com.mycompany.finalprojectclient.models.RequestType;
import com.mycompany.finalprojectclient.models.ResponseData;
import com.mycompany.finalprojectclient.models.ResponseStatus;
import com.mycompany.finalprojectclient.network.ServerConnection;

public class AuthManager {

    private static AuthManager instance;
    private final Gson gson = new Gson();

    private AuthManager() {}

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
            
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                return new ResponseData(ResponseStatus.FAILURE, "No response from server");
            }

            if (!jsonResponse.trim().startsWith("{")) {
                String upperResponse = jsonResponse.toUpperCase();
                
              
                if (upperResponse.contains("ALREADY")) {
                    return new ResponseData(ResponseStatus.FAILURE, "ALREADY_LOGGED_IN");
                }
                
                if (upperResponse.contains("SUCCESS")) {
                    return new ResponseData(ResponseStatus.SUCCESS, "Login successful");
                } else {
                    return new ResponseData(ResponseStatus.FAILURE, jsonResponse);
                }
            }

            return gson.fromJson(jsonResponse, ResponseData.class);
        } catch (IOException | JsonSyntaxException e) {
            return new ResponseData(ResponseStatus.FAILURE, "Error: " + e.getMessage());
        }
    }

    public ResponseData register(String username, String password) {
        RequestData req = new RequestData(RequestType.REGISTER, username, password);
        try {
            String jsonResponse = ServerConnection.getInstance().sendRequest(req);
            
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                return new ResponseData(ResponseStatus.FAILURE, "No response from server");
            }

            if (!jsonResponse.trim().startsWith("{")) {
                if (jsonResponse.toUpperCase().contains("SUCCESS")) {
                    return new ResponseData(ResponseStatus.SUCCESS, "Registration successful");
                } else {
                    return new ResponseData(ResponseStatus.FAILURE, jsonResponse);
                }
            }

            return gson.fromJson(jsonResponse, ResponseData.class);
        } catch (IOException | JsonSyntaxException e) {
            return new ResponseData(ResponseStatus.FAILURE, "Error: " + e.getMessage());
        }
    }
}
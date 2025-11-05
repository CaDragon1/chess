package server.handlers;

import com.google.gson.Gson;
import io.javalin.http.Context;
import models.AuthData;
import models.UserData;
import server.ServerException;
import service.UserService;

import java.util.Map;

public class UserHandler {
    private final UserService service;
    private final Gson serializer = new Gson();

    public UserHandler(UserService service) {
        this.service = service;
    }

    /**
    Format: 1. Parse JSON request body into a data model object
     2. Call a service method using the data model object
     3. Return 200 code on success, or catch errors if raised
     */
    public void handleRegister(Context http) {
        try {
            // 1. Parse request body
            UserData userData = serializer.fromJson(http.body(), UserData.class);

            //2. Call service method
            AuthData authData = service.register(userData);

            // 3. Accept codes and error codes
            http.status(200).json(serializer.toJson(authData));

        } catch (ServerException e) {
            http.status(e.getStatusCode()).json(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (Exception e) {
            http.status(500).json(serializer.toJson(Map.of("message", "Error: unknown error")));
        }
    }

    // apparently a model class is recommended for parsing json like is necessary here. That's why I made LoginRequest.java
    public void handleLogin(Context http) {
        try {
            // 1. Parse request body
            LoginRequest login = serializer.fromJson(http.body(), LoginRequest.class);

            //2. Call service method
            AuthData authData = service.login(login.username(), login.password());

            // 3. Accept codes and error codes
            http.status(200).json(serializer.toJson(authData));
        } catch (ServerException e) {
            System.out.println("LOGIN ERROR: " + e.getMessage() + " " + e.getStatusCode());
            http.status(e.getStatusCode()).json(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (Exception e) {
            http.status(500).json(serializer.toJson(Map.of("message", "Error: unknown error")));
        }
    }

    public void handleLogout(Context http) {
        try {
            // 1. Parse request body
            String authToken = http.header("authorization");
            if (authToken != null && !authToken.isEmpty()) {

                //2. Call service method
                service.logout(authToken);

                // 3. Accept codes and error codes
                http.status(200).json(serializer.toJson(Map.of()));
            } else {
                http.status(401).json(serializer.toJson(Map.of("message", "unauthorized")));
            }
        } catch (ServerException e) {
            http.status(e.getStatusCode()).json(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (Exception e) {
            http.status(500).json(serializer.toJson(Map.of("message", "Error: unknown error")));
        }
    }
}


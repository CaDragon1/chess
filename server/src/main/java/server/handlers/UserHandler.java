package server.handlers;

import io.javalin.http.Context;
import models.AuthData;
import models.UserData;
import org.eclipse.jetty.util.log.Log;
import server.ServerException;
import service.UserService;

import java.util.Map;

public class UserHandler {
    private final UserService service;

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
            System.out.println("Attempting to handle registration");
            // 1. Parse request body
            UserData userData = http.bodyAsClass(UserData.class);
            System.out.println(userData.toString());

            //2. Call service method
            AuthData authData = service.register(userData);
            System.out.println("Service method finished running");
            // 3. Accept codes and error codes
            http.status(200).json(authData);
        } catch (ServerException e) {
            http.status(e.getStatusCode()).json(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            http.status(500).json(Map.of("message", "Error: unknown error"));
        }
    }

    // apparently a model class is recommended for parsing json like is necessary here. That's why I made LoginRequest.java
    public void handleLogin(Context http) {
        try {
            // 1. Parse request body
            LoginRequest login = http.bodyAsClass(LoginRequest.class);
            System.out.println("Login Request: " + login.toString());

            //2. Call service method
            AuthData authData = service.login(login.username(), login.password());
            System.out.println("authData: " + authData.toString());

            // 3. Accept codes and error codes
            http.status(200).json(authData);
        } catch (ServerException e) {
            http.status(e.getStatusCode()).json(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            http.status(500).json(Map.of("message", "Error: unknown error"));
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
                http.status(200).json(Map.of());
            } else {
                http.status(401).json(Map.of("message", "unauthorized"));
            }
        } catch (ServerException e) {
            http.status(e.getStatusCode()).json(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            http.status(500).json(Map.of("message", "Error: unknown error"));
        }
    }
}


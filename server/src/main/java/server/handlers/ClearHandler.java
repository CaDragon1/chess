package server.handlers;

import io.javalin.http.Context;
import server.ServerException;
import service.ClearService;

import java.util.Map;

public class ClearHandler {
    private final ClearService service;

    public ClearHandler(ClearService service) {
        this.service = service;
    }

    public void handleClear(Context http) {
        try {
            // 1. Parse request body
            String authToken = http.header("authorization");

            //2. Call service method
            service.clearData();

            // 3. Accept codes and error codes
            http.status(200).json(Map.of());
        } catch (Exception e) {
            http.status(500).json(Map.of("message", "Error: unknown error"));
        }
    }
}

package server.handlers;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.ClearService;
//import service.ClearService;

import java.util.Map;

public class ClearHandler {
    private final ClearService service;
    private final Gson serializer = new Gson();

    public ClearHandler(ClearService service) {
        this.service = service;
    }

    public void handleClear(Context http) {
        try {
            // 1. Parse request body
            //String authToken = http.header("authorization");

            //2. Call service method
            service.clearData();

            // 3. Accept codes and error codes
            http.status(200).json(serializer.toJson(Map.of()));
        } catch (Exception e) {
            http.status(500).json(serializer.toJson(Map.of("message", "unknown error")));
        }
    }
}

package server.handlers;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.ClearService;

import java.util.Map;

public class ClearHandler {
    private final ClearService service;
    private final Gson serializer = new Gson();

    /**
     * Handler to clear data
     * @param service is the clearservice we're using to clear the data
     */
    public ClearHandler(ClearService service) {
        this.service = service;
    }

    /**
     * handleClear method clears the data within the service object
     * @param http is the context that holds the authtoken that I'm not currently using (TODO)
     */
    public void handleClear(Context http) {
        try {
            service.clearData();
            http.status(200).json(serializer.toJson(Map.of()));
        } catch (Exception e) {
            http.status(500).json(serializer.toJson(Map.of("message", "Error: unknown error")));
        }
    }
}

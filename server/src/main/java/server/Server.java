package server;

import Models.AuthTokenData;
import Models.UserData;
import org.eclipse.jetty.client.HttpResponseException;
import service.Service;
import spark.*;
import com.google.gson.Gson;
import java.util.Map;

public class Server {

    // Create a single Gson object for all Gson operations
    private final Gson gson = new Gson();
    private final Service service;

    public Server(Service service) {
        this.service = service;
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
        Spark.get("/game", this::listGame);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearDatabase);

        Spark.exception(ServerException.class, this::handleException);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }



    private String registerUser(Request request, Response response) throws ServerException{
        try {
            UserData user = new Gson().fromJson(request.body(), UserData.class);
            AuthTokenData authToken = service.register(user);
            response.status(200);
            return gson.toJson(authToken);

        } catch (ServerException e) {
            return handleException(e, response);
        }
    }

    private String loginUser(Request request, Response response) {
        return null;
    }

    private String logoutUser(Request request, Response response) {
        return null;
    }

    private String listGame(Request request, Response response) {
        return null;
    }

    private String createGame(Request request, Response response) {
        return null;
    }

    private String joinGame(Request request, Response response) {
        return null;
    }

    private String clearDatabase(Request request, Response response) {
        return null;
    }

    private String handleException(Exception e, Response response) {
        int statusCode;
        String errorMessage;

        if (e instanceof ServerException) {
            ServerException serverException = (ServerException) e;
            statusCode = serverException.getStatusCode();
            errorMessage = serverException.getMessage();
        }
        else {
            statusCode = 500;
            errorMessage = "Error: " + e.getMessage();
        }

        response.status(statusCode);
        response.type("application/json");
        // Map.of() creates a key-value pair, which Gson can take and turn into Json.
        return gson.toJson(Map.of("message", errorMessage) );
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
package server;

import Models.AuthTokenData;
import Models.UserData;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jetty.client.HttpResponseException;
import service.Service;
import spark.*;
import com.google.gson.Gson;
import java.util.Map;

public class Server {

    // Create a single Gson object for all Gson operations
    private final Gson gson = new Gson();
    private final Service service;

    public Server() {
        service = new Service();
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


    /**
     * registerUser takes the request JSON object, then makes it usable for Java. It then registers the user
     * into the server's database.
     * @param request is the JSON request to register a user
     * @param response is the resulting response JSON object along with the serialized return information
     * @return the AuthTokenData object, serialized as a JSON object.
     * @throws ServerException
     */
    private String registerUser(Request request, Response response) throws ServerException{
        try {
            // Store the user data from the request
            UserData user = new Gson().fromJson(request.body(), UserData.class);
            AuthTokenData authToken = service.register(user);

            response.status(200);
            return gson.toJson(authToken);
        } catch (JsonSyntaxException e) {
            throw new ServerException("bad request", 400);
        }
    }

    private String loginUser(Request request, Response response) throws ServerException{
        /**
         * Small record class specifically for deserializing the login request
         */
        record UserLoginCredentials(String username, String password) {}

        // Store the credentials from the request
        UserLoginCredentials userLogin = new Gson().fromJson(request.body(), UserLoginCredentials.class);
        String username = userLogin.username;
        String password = userLogin.password;

        AuthTokenData authToken = service.login(username, password);
        response.status(200);
        return gson.toJson(authToken);
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

    private String handleException(Exception e, Request request, Response response) {
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
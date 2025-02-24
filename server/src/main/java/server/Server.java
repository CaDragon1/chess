package server;

import spark.*;

public class Server {

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

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }


    private Object registerUser(Request request, Response response) {
        return null;
    }

    private Object loginUser(Request request, Response response) {
        return null;
    }

    private Object logoutUser(Request request, Response response) {
        return null;
    }

    private Object listGame(Request request, Response response) {
        return null;
    }

    private Object createGame(Request request, Response response) {
        return null;
    }

    private Object joinGame(Request request, Response response) {
        return null;
    }

    private Object clearDatabase(Request request, Response response) {
        return null;
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}

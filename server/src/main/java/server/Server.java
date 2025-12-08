package server;
import dataaccess.SqlAuthDataAccess;
import dataaccess.SqlGameDataAccess;
import dataaccess.SqlUserDataAccess;
import dataaccess.websocket.WebSocketHandler;
import io.javalin.*;
import server.handlers.ClearHandler;
import server.handlers.GameHandler;
import server.handlers.UserHandler;
import service.ClearService;
import service.GameService;
import service.UserService;

public class Server {
    private final Javalin javalin;
    /**
     * Server constructor to set up the data access objects, endpoints, server, and websockethandler
     */
    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        SqlUserDataAccess userDAO = new SqlUserDataAccess();
        SqlAuthDataAccess authDAO = new SqlAuthDataAccess();
        SqlGameDataAccess gameDAO = new SqlGameDataAccess();

        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);
        ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);

        // Register your endpoints and exception handlers here.
        javalin.post("/user", new UserHandler(userService)::handleRegister);
        javalin.post("/session", new UserHandler(userService)::handleLogin);
        javalin.delete("/session", new UserHandler(userService)::handleLogout);
        javalin.get("/game", new GameHandler(gameService)::handleListGames);
        javalin.post("/game", new GameHandler(gameService)::handleCreateGame);
        javalin.put("/game", new GameHandler(gameService)::handleJoinGame);
        javalin.delete("/db", new ClearHandler(clearService)::handleClear);

        WebSocketHandler wsHandler = new WebSocketHandler(gameService, userService);
        wsHandler.registerHandlers(javalin);
    }

    /**
     * Run function for the desired port for a javalin server
     * @param desiredPort an int for the desited port to run javalin on
     * @return the integer of the port used when starting javalin
     */
    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    /**
     * Stop function to halt the javalin server
     */
    public void stop() {
        javalin.stop();
    }
}

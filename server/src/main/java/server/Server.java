package server;
import com.google.gson.Gson;
import dataaccess.SqlAuthDataAccess;
import dataaccess.SqlGameDataAccess;
import dataaccess.SqlUserDataAccess;
import io.javalin.*;
import io.javalin.websocket.WsMessageContext;
import server.handlers.ClearHandler;
import server.handlers.GameHandler;
import server.handlers.UserHandler;
import service.ClearService;
import service.GameService;
import service.UserService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();
    private final Map<Integer, Set<WsMessageContext>> gameSessions = new ConcurrentHashMap<>();

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
    }



    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}

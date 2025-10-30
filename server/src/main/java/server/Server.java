package server;
import com.fasterxml.jackson.databind.ObjectMapper;
import dataaccess.memorydao.MemoryAuthDataAccess;
import dataaccess.memorydao.MemoryGameDataAccess;
import dataaccess.memorydao.MemoryUserDataAccess;
import io.javalin.*;
import models.AuthData;
import server.handlers.ClearHandler;
import server.handlers.GameHandler;
import server.handlers.UserHandler;
import service.ClearService;
import service.GameService;
import service.UserService;

public class Server {

    private final Javalin javalin;
    private UserService userService;
    private GameService gameService;
    private ClearService clearService;

    private MemoryUserDataAccess userDAO;
    private MemoryAuthDataAccess authDAO;
    private MemoryGameDataAccess gameDAO;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        userDAO = new MemoryUserDataAccess();
        authDAO = new MemoryAuthDataAccess();
        gameDAO = new MemoryGameDataAccess();

        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO, userDAO);
        clearService = new ClearService(userDAO, gameDAO, authDAO);

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

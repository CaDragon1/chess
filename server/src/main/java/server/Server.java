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
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();

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

        // Websocket setup
        javalin.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                ctx.enableAutomaticPings();
                System.out.println("Websocket connected");
            });
            ws.onMessage(ctx -> {
                String json = ctx.message();
                ctx.send("WebSocket response:" + json);
                UserGameCommand command = gson.fromJson(json, UserGameCommand.class);
                // Parse JSON into commands such as CONNECT, MAKE_MOVE, LEAVE, RESIGN with a switch statement
                switch (command.getCommandType()) {
                    case CONNECT -> handleConnect(ctx, command);
                    case LEAVE -> handleLeave(ctx, command);
                    case RESIGN -> handleResign(ctx, command);
                    case MAKE_MOVE -> handleMakeMove(ctx, command);
                }
            });
            ws.onClose(ctx -> {
                System.out.println("Websocket closed");
                // Remove session
            });
        });
    }

    // Handler functions for my json parsing in websocket

    /**
     * handleMakeMove json structure:
     * {
     *   "commandType": "MAKE_MOVE",
     *   "authToken": "token",
     *   "gameID": int,
     *   "move": { "start": { "row": 3, "col": 3 }, "end": { "row": 5, "col": 5 } }
     * }
     * @param ctx
     * @param command
     */
    private void handleMakeMove(WsMessageContext ctx, UserGameCommand command) {
        ServerMessage makeMove = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
    }

    private void handleResign(WsMessageContext ctx, UserGameCommand command) {
    }

    private void handleLeave(WsMessageContext ctx, UserGameCommand command) {
    }

    private void handleConnect(WsMessageContext ctx, UserGameCommand command) {
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}

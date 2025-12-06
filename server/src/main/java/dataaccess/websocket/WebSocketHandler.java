package dataaccess.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.Javalin;
import io.javalin.websocket.WsMessageContext;
import models.GameData;
import service.GameService;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {
    private final GameService gameService;
    private final UserService userService;
    private final Map<Integer, Set<WsMessageContext>> gameSessions = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public WebSocketHandler(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    public void registerHandlers(Javalin javalin) {
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

    // Handler functions for my json parsing from websocket's message field

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
    // Update database with new move, broadcast the board, notify others
    private void handleMakeMove(WsMessageContext ctx, UserGameCommand command) {
        ServerMessage makeMove = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
    }

    // Signal game over state for game in db, notify others
    private void handleResign(WsMessageContext ctx, UserGameCommand command) {
    }

    // Unregister ctx, notify others
    private void handleLeave(WsMessageContext ctx, UserGameCommand command) {
    }

    // Register context, send load game to client, notify others
    private void handleConnect(WsMessageContext ctx, UserGameCommand command) {
        try {
            int gameID = command.getGameID();
            String authToken = command.getAuthToken();
            if (authToken == null) {
                sendError(ctx, "Error: unauthorized");
                // return statement so I'm not nesting a ton of code in an else statement
                return;
            }
            int gameID = command.getGameID();
            GameData gameData = game

            // making a new key set for the game id and putting it in gamesessions, then adding websocket message
            gameSessions.computeIfAbsent(gameID, id -> ConcurrentHashMap.newKeySet()).add(ctx);
        } catch (DataAccessException ignored) {

        }
    }

    // Function to send error messages from server to the client. Must contain the text "Error:"
    private void sendError(WsMessageContext ctx, String error) {
        ServerMessage errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        errorMsg.errorMsg = error;
        ctx.send(gson.toJson(errorMsg));
    }

    private void broadcast
}

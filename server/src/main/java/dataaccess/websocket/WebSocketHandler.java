package dataaccess.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.Javalin;
import io.javalin.websocket.WsMessageContext;
import models.AuthData;
import models.GameData;
import server.ServerException;
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
        try {
            // get and verify the auth data and game
            String authToken = command.getAuthToken();
            int gameID = command.getGameID();

            AuthData authData = userService.getAuthDataFromToken(authToken);
            if (authData == null) {
                sendError(ctx, "Error: unauthorized");
                return;
            }
            GameData gameData = gameService.getGame(gameID);
            if (gameData == null) {
                sendError(ctx, "Error: game not found");
                return;
            }


            ServerMessage makeMove = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    // Signal game over state for game in db, notify others
    private void handleResign(WsMessageContext ctx, UserGameCommand command) {
    }

    // Unregister ctx, notify others
    private void handleLeave(WsMessageContext ctx, UserGameCommand command) {
    }

    /**
     * Register context, send load game to client, notify others
     * @param ctx is the websocket that is attempting connection
     * @param command is the command object containing the command being passed (wow)
     */
    private void handleConnect(WsMessageContext ctx, UserGameCommand command) {
        try {
            // get and verify the auth data and game
            String authToken = command.getAuthToken();
            int gameID = command.getGameID();

            AuthData authData = userService.getAuthDataFromToken(authToken);
            if (authData == null) {
                sendError(ctx, "Error: unauthorized");
                return;
            }

            GameData gameData = gameService.getGame(gameID);
            if (gameData == null) {
                sendError(ctx, "Error: game not found");
                return;
            }

            // making a new key set for the game id and putting it in gamesessions, then adding websocket message
            gameSessions.computeIfAbsent(gameID, id -> ConcurrentHashMap.newKeySet()).add(ctx);

            // send to client
            ServerMessage loadingMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadingMessage.game = gameData;
            ctx.send(gson.toJson(loadingMessage));

            // determine team
            String team;
            if (authData.username().equals(gameData.whiteUsername())) {
                team = "white";
            }
            else if (authData.username().equals(gameData.blackUsername())) {
                team = "black";
            } else {
                team = "observer";
            }

            // send to everyone else's clients
            String broadcastMessage = notification(authData.username() + "has joined team " + team);
            broadcastToOthers(gameID, ctx, broadcastMessage);

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    /**
     * Function to send error messages from server to the client. Must contain the text "Error:"
     * @param ctx is the client's websocket we're sending the message to
     * @param error is the error message, which gets converted to json
     */
    private void sendError(WsMessageContext ctx, String error) {
        ServerMessage errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        errorMsg.errorMsg = error;
        ctx.send(gson.toJson(errorMsg));
    }

    /**
     * Function for creating json objects for notification messages
     * @param message is the message we want to turn into a json servermessage string
     * @return the json version of servermessage with message as the notification
     */
    private String notification(String message) {
        ServerMessage notify = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notify.notification = message;
        return gson.toJson(notify);
    }

    /**
     * this version of my broadcast function broadcasts to everyone.
     * Useful for load game, check, and checkmate.
     * @param gameID is the game whose id we're looking at
     * @param message is the json message being sent
     */
    private void broadcastToAll(int gameID, String message) {
        Set<WsMessageContext> currentSessions = gameSessions.get(gameID);
        if (currentSessions != null) {
            for (WsMessageContext context : currentSessions) {
                context.send(message);
            }
        }
    }

    /**
     * this version of my broadcast function should skip the current websocket connection (the main client, per se).
     * Useful for join and move notifications
     * @param gameID is the game whose id we're looking at
     * @param ctx is the websocket of the "main client"
     * @param message is the json message being sent
     */
    private void broadcastToOthers(int gameID, WsMessageContext ctx, String message) {
        Set<WsMessageContext> currentSessions = gameSessions.get(gameID);
        if (currentSessions != null) {
            for (WsMessageContext context : currentSessions) {
                if (context != ctx) {
                    context.send(message);
                }
            }
        }
    }
}

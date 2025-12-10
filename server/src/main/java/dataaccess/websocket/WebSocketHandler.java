package dataaccess.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.websocket.WsMessageContext;
import models.AuthData;
import models.GameData;
import org.eclipse.jetty.websocket.api.Session;
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
    private final Map<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();
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
                try {
                    String json = ctx.message();
                    UserGameCommand command = gson.fromJson(json, UserGameCommand.class);
                    // Parse JSON into commands such as CONNECT, MAKE_MOVE, LEAVE, RESIGN with a switch statement
                    switch (command.getCommandType()) {
                        case CONNECT -> handleConnect(ctx, command);
                        case LEAVE -> handleLeave(ctx, command);
                        case RESIGN -> handleResign(ctx, command);
                        case MAKE_MOVE -> handleMakeMove(ctx, command);
                    }
                } catch (Exception e) {
                    sendError(ctx, "Invalid Json");
                }
            });
            ws.onClose(ctx -> {
                System.out.println("Websocket closed");
                // Remove session
                gameSessions.values().forEach(gameSessions -> gameSessions.remove(ctx.session));
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
     * @param ctx contains the sender's websocket info
     * @param command contains the authtoken and gameid we need
     */
    // Update database with new move, broadcast the board, notify others
    private void handleMakeMove(WsMessageContext ctx, UserGameCommand command) {
        try {
            // get and verify the auth data and game
            String authToken = command.getAuthToken();
            int gameID = command.getGameID();

            AuthData authData = userService.getAuthDataFromToken(authToken);
            if (authData == null) {
                sendError(ctx, "unauthorized");
                return;
            }
            GameData gameData = gameService.getGame(gameID);
            if (gameData == null) {
                sendError(ctx, "game not found");
                return;
            }

            ChessMove move = command.getMove();
            GameData updatedGame = gameService.makeMove(authToken, gameID, move);

            ServerMessage makeMove = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            makeMove.game = updatedGame;
            broadcastToAll(gameID, gson.toJson(makeMove));

            String moveMessage = authData.username() + " moved " + move.getStartPosition().getCoordinates() + " to " +
                    move.getEndPosition().getCoordinates();

            // State-based notifications
            broadcastStatus(ctx, updatedGame, gameID, moveMessage);

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    private void broadcastStatus(WsMessageContext ctx, GameData updatedGame, int gameID, String moveMessage) {
        GameData.GameStatus status = updatedGame.status();
        ChessGame chessGame = updatedGame.getGame();

        if (status == GameData.GameStatus.WHITE_WIN) {
            broadcastToAll(gameID, notification("Checkmate! White wins!"));
        } else if (status == GameData.GameStatus.BLACK_WIN) {
            broadcastToAll(gameID, notification("Checkmate! Black wins!"));
        } else if (status == GameData.GameStatus.STALEMATE) {
            broadcastToAll(gameID, notification("Stalemate! The game is a draw."));
        } else {
            // check state of next team
            ChessGame.TeamColor toMove = chessGame.getTeamTurn();
            if (chessGame.isInCheck(toMove)) {
                String userInCheck = toMove == ChessGame.TeamColor.WHITE
                        ? updatedGame.whiteUsername()
                        : updatedGame.blackUsername();
                broadcastToAll(gameID,
                        notification(userInCheck + " is in check!"));
            }
        }

        // Always tell others about the move
        broadcastToOthers(gameID, ctx, notification(moveMessage));
    }


    // Signal game over state for game in db, notify others
    private void handleResign(WsMessageContext ctx, UserGameCommand command) {
        try {
            // get and verify the auth data and game
            String authToken = command.getAuthToken();
            int gameID = command.getGameID();

            AuthData authData = userService.getAuthDataFromToken(authToken);
            if (authData == null) {
                sendError(ctx, "unauthorized");
                return;
            }

            GameData gameData = gameService.getGame(gameID);
            if (gameData == null) {
                sendError(ctx, "game not found");
                return;
            }

            boolean isPlayer = authData.username().equals(gameData.whiteUsername()) ||
                    authData.username().equals(gameData.blackUsername());
            if (!isPlayer) {
                sendError(ctx, "Player is observer and cannot resign");
                return;
            }

            if (gameData.status() != GameData.GameStatus.LIVE && gameData.status() != GameData.GameStatus.PREGAME) {
                sendError(ctx, "Game is over!");
                return;
            }

            gameService.resignGame(authToken, gameID);

            String message = authData.username() + " has resigned. Game over!";
            broadcastToAll(gameID, notification(message));

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    // Unregister ctx, notify others
    private void handleLeave(WsMessageContext ctx, UserGameCommand command) {
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

            Set<Session> sessions = gameSessions.get(gameID);
            if(sessions != null) {
                sessions.remove(ctx.session);
            }

            if (authData.username().equals(gameData.whiteUsername())) {
                gameService.updateGame(removeUser("WHITE", gameData));
            }
            else if (authData.username().equals(gameData.blackUsername())) {
                gameService.updateGame(removeUser("BLACK", gameData));
            }

            String message = authData.username() + " has left the game.";
            broadcastToOthers(gameID, ctx, notification(message));

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private GameData removeUser(String team, GameData gameData) {
        if (!team.equalsIgnoreCase("white") && !team.equalsIgnoreCase("black")) {
            throw new RuntimeException("Error: wrong team color");
        }
        return new GameData(
                gameData.gameID(),
                team.equalsIgnoreCase("white") ? null : gameData.whiteUsername(),
                team.equalsIgnoreCase("black") ? null : gameData.blackUsername(),
                gameData.gameName(),
                gameData.getGame(),
                gameData.status()
        );
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
            gameSessions.computeIfAbsent(gameID, id -> ConcurrentHashMap.newKeySet()).add(ctx.session);

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
            String broadcastMessage = notification(authData.username() + " has joined as " + team);
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
        errorMsg.errorMessage = error;
        ctx.send(gson.toJson(errorMsg));
    }

    /**
     * Function for creating json objects for message messages
     * @param message is the message we want to turn into a json servermessage string
     * @return the json version of servermessage with message as the message
     */
    private String notification(String message) {
        ServerMessage notify = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notify.message = message;
        return gson.toJson(notify);
    }

    /**
     * this version of my broadcast function broadcasts to everyone.
     * Useful for load game, check, and checkmate.
     * @param gameID is the game whose id we're looking at
     * @param message is the json message being sent
     */
    private void broadcastToAll(int gameID, String message) {
        Set<Session> currentSessions = gameSessions.get(gameID);
        if (currentSessions != null) {
            // check if session is open, cycle through each one, and attempt to send the message
            currentSessions.stream()
                    .filter(Session::isOpen)
                    .forEach(session -> {
                try {
                    session.getRemote().sendString(message);
                } catch (Exception e) {
                    session.close(); // session is dead so should be excised from our set
                }
            });
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
        Set<Session> currentSessions = gameSessions.get(gameID);
        Session senderSession = ctx.session;

        if (currentSessions != null) {
            // check if session is open and not the sender, cycle through each one, and attempt to send the message
            currentSessions.stream()
                    .filter(session -> session.isOpen() && session != senderSession)
                    .forEach(session -> {
                        try {
                            session.getRemote().sendString(message);
                        } catch (Exception e) {
                            session.close(); // session is dead so should be excised from our set
                        }
                    });
        }
    }
}

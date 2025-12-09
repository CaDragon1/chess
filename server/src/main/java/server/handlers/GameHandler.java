package server.handlers;

import chess.ChessMove;
import com.google.gson.Gson;
import io.javalin.http.Context;
import models.GameData;
import server.ServerException;
import service.GameService;

import java.util.Collection;
import java.util.Map;

public class GameHandler {
    private final GameService service;
    private final Gson serializer = new Gson();

    public GameHandler(GameService service) {
        this.service = service;
    }

    public void handleListGames(Context http) {
        try {
            // 1. Parse request body
            String authToken = http.header("authorization");
            if (authToken != null && !authToken.isEmpty()) {

                //2. Call service method
                Collection<GameData> gameList = service.listGames(authToken);

                // 3. Accept codes and error codes
                http.status(200).json(serializer.toJson(Map.of("games", gameList)));
            } else {
                http.status(401).json(serializer.toJson(Map.of("message", "unauthorized")));
            }
        } catch (ServerException e) {
            http.status(e.getStatusCode()).json(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (Exception e) {
            http.status(500).json(serializer.toJson(Map.of("message", "Error: unknown error")));
        }
    }

    public void handleCreateGame(Context http) {
        try {
            // 1. Parse request body
            String authToken = http.header("authorization");
            CreateGameRequest request = serializer.fromJson(http.body(), CreateGameRequest.class);
            String name = request.gameName();

            if (authToken == null || authToken.isBlank()) {
                http.status(401).json(serializer.toJson(Map.of("message", "Error: unauthorized")));
            }
            else if (name == null || name.isBlank()) {
                http.status(400).json(serializer.toJson(Map.of("message", "Error: bad request")));
            } else {
                //2. Call service method
                int index = service.createGame(authToken, name);

                // 3. Accept codes and error codes
                http.status(200).json(serializer.toJson(Map.of("gameID", index)));
            }
        } catch (ServerException e) {
            http.status(e.getStatusCode()).json(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (Exception e) {
            http.status(500).json(serializer.toJson(Map.of("message", "Error: unknown error")));
        }
    }

    public void handleJoinGame(Context http) {
        try {
            String authToken = http.header("authorization");
            JoinGameRequest request = serializer.fromJson(http.body(), JoinGameRequest.class);
            String team = request.playerColor();
            int gameID = request.gameID();

            GameData gameData = service.getGame(gameID);
            if (gameData == null) {
                http.status(404).json(serializer.toJson(Map.of("message", "Error: Game not found")));
                return;
            }
            if (!joinableGamestate(gameData.status())) {
                http.status(400).json(serializer.toJson(Map.of("message", "Error: Cannot join a completed game")));
                return;
            }

            service.joinGame(authToken, team, gameID);
            http.status(200).json(serializer.toJson(Map.of("gameID", gameID)));

        } catch (ServerException e) {
            http.status(e.getStatusCode()).json(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (Exception e) {
            http.status(500).json(serializer.toJson(Map.of("message", "Error: unknown error")));
        }
    }

    public void handleMakeMove(Context http) {
        try {
            int gameID = http.pathParamAsClass("gameID", Integer.class).get();

            String authToken = http.header("authorization");
            if (authToken == null || authToken.isBlank()) {
                http.status(401).json(serializer.toJson(Map.of("message", "Error: unauthorized")));
                return;
            }

            MakeMoveRequest request = serializer.fromJson(http.body(), MakeMoveRequest.class);
            ChessMove move = request.move();

            GameData gameData = service.getGame(gameID);
            if (gameData == null) {
                http.status(404).json(serializer.toJson(Map.of("message", "Error: Game not found")));
                return;
            }
            if (!playableGamestate(gameData.status())) {
                http.status(400).json(serializer.toJson(Map.of("message", "Error: Game is not live")));
                return;
            }

            service.makeMove(authToken, gameID, move);
            http.status(200).json(serializer.toJson(Map.of("message", "Move successful")));
        }
        catch (ServerException e) {
            http.status(e.getStatusCode()).json(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
        catch (Exception e) {
            http.status(500).json(serializer.toJson(Map.of("message", "Error: unknown error")));
        }
    }

    private boolean playableGamestate(GameData.GameStatus status) throws ServerException {
        return status == GameData.GameStatus.LIVE;
    }

    private boolean joinableGamestate(GameData.GameStatus status) throws ServerException {
        return status == GameData.GameStatus.LIVE || status == GameData.GameStatus.PREGAME;
    }
}

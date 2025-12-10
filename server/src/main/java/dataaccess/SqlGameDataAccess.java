package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.interfaces.GameDataAccess;
import models.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// "Would you rather have unlimited bacon, but no games... Or games, UNLIMITED games, and no games?" -Jschlatt
public class SqlGameDataAccess implements GameDataAccess, SqlAccess {

    public SqlGameDataAccess () {
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<GameData>();
        String list = "SELECT * FROM GameData ORDER BY gameID";

        try (var connection = DatabaseManager.getConnection()) {
            var preparedStatement = connection.prepareStatement(list);
            var response = preparedStatement.executeQuery();

            while (response.next()) {
                games.add(parseGameData(response));
            }
        } catch (SQLException e) {
            throw new DataAccessException("List games failure: " + e.getMessage());
        }
        return games;
    }

    // Helper function for listing games
    private GameData parseGameData(ResultSet response) throws DataAccessException {
        try {
            int gameID = response.getInt("gameID");
            String white = response.getString("whiteUsername");
            if (response.wasNull()) {
                white = null;
            }
            String black = response.getString("blackUsername");
            if (response.wasNull()) {
                black = null;
            }

            String name = response.getString("gameName");
            String game = response.getString("game");

            ChessGame chessGame = new Gson().fromJson(game, ChessGame.class);
            System.out.println("!!!!! Setting status to null");
            GameData.GameStatus status = null;
            String gameStatus = response.getString("status");
            System.out.println("!!!!! gameStatus from response is " + gameStatus);
            if (!response.wasNull() && gameStatus != null) {
                try {
                    status = GameData.GameStatus.valueOf(gameStatus.toUpperCase());
                    System.out.println("!!!!! status verified as " + status.toString());
                } catch (IllegalArgumentException ignored) {
                    // apparently it's okay to just ignore something like this. I'm just ensuring that if status doesn't exist all is well
                }
            }

            if (status == null) {
                System.out.println("!!!! PREGAMING STATUS");
                status = GameData.GameStatus.PREGAME;
            }

            return new GameData (gameID, white, black, name, chessGame, status);
        } catch (Exception e) {
            throw new DataAccessException("Json is invalid: " + e.getMessage());
        }
    }

    private static GameData.GameStatus getGameStatus(ChessGame game, GameData currentData) {
        if (currentData.status() == GameData.GameStatus.RESIGNED) {
            return GameData.GameStatus.RESIGNED;
        }
        if (game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInStalemate(ChessGame.TeamColor.BLACK)) {
            return GameData.GameStatus.STALEMATE;
        }
        ChessGame.TeamColor currentTurn = game.getTeamTurn();
        if (game.isInCheckmate(currentTurn)) {
            return currentTurn == ChessGame.TeamColor.WHITE ?
                    GameData.GameStatus.BLACK_WIN : GameData.GameStatus.WHITE_WIN;
        }
        if (currentData.blackUsername() == null || currentData.whiteUsername() == null) {
            return GameData.GameStatus.PREGAME;
        }

        return GameData.GameStatus.LIVE;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            String fetch = "SELECT * FROM GameData WHERE gameID = ?";

            try (var preparedStatement = connection.prepareStatement(fetch)) {
                preparedStatement.setInt(1, gameID);

                try (var response = preparedStatement.executeQuery()) {
                    if (!response.next()) {
                        return null;
                    }
                    return parseGameData(response);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Get game failure: " + e.getMessage());
        }
    }

    @Override
    public void createGame(GameData gameData) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            String create = "INSERT INTO GameData (gameID, whiteUsername, blackUsername, gameName, game, status)" +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            Gson gson = new Gson();

            try (var preparedStatement = connection.prepareStatement(create)) {
                preparedStatement.setInt(1, gameData.gameID());
                preparedStatement.setObject(2,gameData.whiteUsername());
                preparedStatement.setObject(3, gameData.blackUsername());
                preparedStatement.setString(4, gameData.gameName());

                // Serialize the game data
                String gameJson = gson.toJson(gameData.game());
                preparedStatement.setString(5, gameJson);

                GameData.GameStatus status = gameData.status();
                preparedStatement.setString(6, status != null ? status.name() : GameData.GameStatus.PREGAME.name());

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Create game failure: " + e.getMessage());
        }
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            String update = "UPDATE GameData SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ?, status = ?" +
                    " WHERE gameID = ?";
            Gson gson = new Gson();

            try (var preparedStatement = connection.prepareStatement(update)) {
                preparedStatement.setObject(1,gameData.whiteUsername());
                preparedStatement.setObject(2, gameData.blackUsername());
                preparedStatement.setString(3, gameData.gameName());

                String updateJson = gson.toJson(gameData.game());
                preparedStatement.setString(4, updateJson);

                GameData.GameStatus status = gameData.status();
                preparedStatement.setString(5, status != null ? status.name() : GameData.GameStatus.PREGAME.name());

                preparedStatement.setInt(6, gameData.gameID());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Update game failure: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            var clear = "DELETE FROM GameData";

            try (var preparedStatement = connection.prepareStatement(clear)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("GameData clear failed: " + e.getMessage());
        }
    }

    private final String[] createStatements = {
            """
                CREATE TABLE IF NOT EXISTS GameData (
                `gameID` INT UNSIGNED PRIMARY KEY,
                `whiteUsername` VARCHAR(255),
                `blackUsername` VARCHAR(255),
                `gameName` VARCHAR(255) NOT NULL,
                `game` TEXT NOT NULL,
                `status` VARCHAR(20) NOT NULL DEFAULT 'PREGAME',
                FOREIGN KEY (whiteUsername) REFERENCES UserData(username) ON DELETE SET NULL,
                FOREIGN KEY (blackUsername) REFERENCES UserData(username) ON DELETE SET NULL
                );"""
    };

    @Override
    public int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
                return preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Game DAO update failure: " + e.getMessage());
        }
    }

    @Override
    public void configureDatabase() throws DataAccessException {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}

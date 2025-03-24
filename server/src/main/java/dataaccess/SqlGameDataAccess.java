package dataaccess;

import chess.ChessGame;
import models.AuthTokenData;
import models.GameData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class SqlGameDataAccess implements GameDataAccess, SqlAccess {
    @Override
    public Collection<GameData> getGameList() {
        return List.of();
    }

    @Override
    public GameData getGameByName(String gameName) {
        return null;
    }

    @Override
    public GameData getGameByID(int gameID) {
        return null;
    }

    @Override
    public void createGame(GameData gameData) {

    }

    @Override
    public void joinGame(AuthTokenData authData, ChessGame.TeamColor team, int gameID) {

    }

    @Override
    public void clearGames() {

    }

    @Override
    public int executeUpdate(String statement, Object... params) throws server.ServerException {
        return 0;
    }

    private final String[] createStatements = {
            """
                CREATE TABLE IF NOT EXISTS GameData (
                `gameID` INT UNSIGNED PRIMARY KEY,
                `whiteUsername` VARCHAR(255),
                `blackUsername` VARCHAR(255),
                `gameName` VARCHAR(255) NOT NULL,
                `game` TEXT NOT NULL,
                FOREIGN KEY (whiteUsername) REFERENCES UserData(username) ON DELETE SET NULL,
                FOREIGN KEY (blackUsername) REFERENCES UserData(username) ON DELETE SET NULL
                );"""
    };

    @Override
    public void configureDatabase() throws ServerException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new ServerException(e.getMessage());
        }

    }
}

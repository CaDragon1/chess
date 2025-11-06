package dataaccess;

import dataaccess.interfaces.GameDataAccess;
import models.GameData;
import server.ServerException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class SqlGameDataAccess implements GameDataAccess, SqlAccess {
    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public void createGame(GameData gameData) throws DataAccessException {

    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

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
    public int executeUpdate(String statement, Object... params) throws ServerException {
        return 0;
    }

    @Override
    public void configureDatabase() throws ServerException {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new ServerException(e.getMessage(), 500);
        }
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new ServerException(e.getMessage(), 500);
        }
    }
}

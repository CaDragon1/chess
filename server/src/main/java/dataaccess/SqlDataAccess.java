package dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class SqlDataAccess implements SqlAccess {

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
            throw new DataAccessException("DAO update failure: " + e.getMessage());
        }
    }

    @Override
    public abstract void configureDatabase() throws DataAccessException;
}
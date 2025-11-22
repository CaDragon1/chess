package dataaccess;

public interface SqlAccess {
    int executeUpdate(String statement, Object... params) throws DataAccessException;
    void configureDatabase() throws DataAccessException;
}

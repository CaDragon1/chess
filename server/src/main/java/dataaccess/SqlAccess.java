package dataaccess;

public interface SqlAccess {
    int executeUpdate(String statement, Object... params) throws server.ServerException, ServerException;
    void configureDatabase() throws ServerException;
}

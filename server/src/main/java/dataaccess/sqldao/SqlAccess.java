package dataaccess.sqldao;

import server.ServerException;

public interface SqlAccess {
    int executeUpdate(String statement, Object... params) throws ServerException;
    void configureDatabase() throws ServerException;
}

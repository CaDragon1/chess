package ui;

import client.ChessClient;
import exception.ResponseException;

public abstract class BaseUI implements UIState{
    protected final ChessClient client;
    public UIStatesEnum state;

    public BaseUI(ChessClient client) {
        this.client = client;
    }

    public void validateParameterLength(String[] params, int expectedLength) throws ResponseException {
        if (params.length < expectedLength) {
            throw new ResponseException("Parameters missing", 400);
        }
        else if (params.length > expectedLength) {
            throw new ResponseException("Too many parameters given", 400);
        }
    }
}

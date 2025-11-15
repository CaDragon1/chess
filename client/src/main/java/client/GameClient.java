package client;

import exception.ResponseException;

public class GameClient implements Client {
    @Override
    public String help() {
        return "";
    }

    @Override
    public String eval(String input) throws ResponseException {
        return "";
    }
}

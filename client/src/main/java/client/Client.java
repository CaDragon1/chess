package client;

import exception.ResponseException;

public interface Client {
    String help();
    String eval(String input) throws ResponseException;
}

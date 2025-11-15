package client;

import exception.ResponseException;

import java.util.Arrays;

public class PreLoginClient implements Client{
    public PreLoginClient(String serverURL, Repl repl) {
    }

    public boolean help() {
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch(cmd) {
                case "register" -> registerUser(params);
                //case "log in"
            }
        }
    }

    private String registerUser(String[] params) throws ResponseException {
    }
}

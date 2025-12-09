package client;

import exception.ResponseException;
import models.AuthData;
import models.UserData;

import java.util.Arrays;

public class PreLoginClient implements Client{
    private final ServerFacade server;

    public PreLoginClient(ServerFacade server) {
        this.server = server;
    }

    /**
     * Method to return a json-formatted string for "help" command
     * @return json-formatted string containing the message for the help list
     */
    public String help() {
        return "{\"message\":\"--- HELP ---\\nCommands:\\nregister <username> <password> <email>\\nlogin <username> <password>\\nhelp\\nquit\"}";
    }

    /**
     * Method to evaluate an input and determine which action the user wants to make
     * @param input is the inputted string
     * @return a json-formatted string depending on the result of the input
     */
    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch(cmd) {
                case "register" -> registerUser(params);
                case "login" -> loginUser(params);
                case "help" -> help();
                case "quit" -> "{\"message\":\"Quitting application...\"}";
                default -> "{\"message\":\"Error: Unknown command. Type 'help' for a list of available commands.\"}";
            };
        } catch (ResponseException e) {
            return String.format("{\"status\":\"error\",\"message\":\"%s\"}", e.getMessage());
        }
    }

    /**
     * Verify and execute the login of a user
     * @param params contains username and password; if it doesn't, an exception is thrown
     * @return the result of user login as a json-formatted string
     * @throws ResponseException if the parameters are insufficient
     */
    private String loginUser(String[] params) throws ResponseException {
        if (params.length < 2) {
            throw new ResponseException("Missing username or password", 400);
        }
        if (params.length > 2) {
            throw new ResponseException("Too many parameters; Spaces not allowed in username or password", 400);
        }

        AuthData authData = server.loginUser(params[0], params[1]);
        // reformatted the return value so that I can pass the authToken safely
        return String.format("{\"status\":\"success\", \"message\":\"%s successfully logged in\", \"authToken\":\"%s\"}",
                params[0], authData.authToken());
    }

    /**
     * Function to register a user based on input
     * @param params include username, password, and email. Otherwise, throws like my team does in Marvel Rivals when they instalock 4 dps
     * @return a json string indicating success
     * @throws ResponseException if the user creation is not possible given parameters (or something else fails)
     */
    private String registerUser(String[] params) throws ResponseException {
        if (params.length < 3) {
            throw new ResponseException("Missing username, password, or email", 400);
        }
        if (params.length > 3) {
            throw new ResponseException("Too many parameters; Spaces not allowed in username, password, or email", 400);
        }

        UserData user = new UserData(params[0], params[1], params[2]);

        // Set auth token in cached data object
        AuthData authData = server.registerUser(user);

        return String.format("{\"status\":\"success\", \"message\":\"%s successfully registered\", \"authToken\":\"%s\"}",
                params[0], authData.authToken());
    }
}

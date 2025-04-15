package ui;

import client.ChessClient;
import exception.ResponseException;

public class PreloginUI extends BaseUI {
    public PreloginUI(ChessClient client) {
        super(client);
        state = UIStatesEnum.PRELOGINUI;
    }

    @Override
    public String handler(String input) throws ResponseException {
        String[] tokens = input.split(" ");
        return switch (tokens[0].toLowerCase()) {
            case "register" -> register(tokens);
            case "login" -> login(tokens);
            case "quit" -> "quit";
            default -> displayHelpInfo();
        };
    }

    private String register(String[] tokens) throws ResponseException {
        validateParameterLength(tokens, 4);
        return client.register(tokens[1], tokens[2], tokens[3]);
    }

    private String login(String[] tokens) throws ResponseException {
        validateParameterLength(tokens, 3);
        return client.login(tokens[1], tokens[2]);
    }

    @Override
    public String displayHelpInfo() {
        return """
                --- HELP ---
                Type a command to get the corresponding action.
                - register [username] [password] [email] | Register a new user.
                - login [username] [password]            | Login an existing user.
                - quit                                   | Quit the application.
                - help                                   | Display this help menu.
                """;
    }
}

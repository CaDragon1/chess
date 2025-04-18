package ui;

import client.ChessClient;
import exception.ResponseException;
import exception.UIStateException;

public class PreloginUI extends BaseUI {
    public PreloginUI(ChessClient client) {
        super(client);
        state = UIStatesEnum.PRELOGINUI;
    }

    @Override
    public String handler(String input) throws ResponseException {
        String[] tokens = input.split(" ");
        switch (tokens[0].toLowerCase()) {
            case "register" ->  {
                register(tokens);
            }
            case "login" -> {
                login(tokens);
            }
            case "quit" -> {return "quit";}
            default -> {return displayHelpInfo();}
        };
        return null;
    }

    /**
     * register will take the tokens inputted, call the client's register function with them, and then throw a new
     * UIStateException. This UIStateException will then introduce the next UIState.
     * @param tokens inputted parameters
     * @throws ResponseException errors if they come up. Otherwise,
     * @throws UIStateException as the success state, which will keep the scanner object open and seamlessly continue
     * to the next UI state.
     */
    private void register(String[] tokens) throws ResponseException {
        validateParameterLength(tokens, 4);
        String register = client.register(tokens[1], tokens[2], tokens[3]);
        throw new UIStateException(new PostloginUI(client), register);
    }

    /**
     * login acts much the same as register. It will take the tokens inputted, call the client's login function with them,
     * and then throw a new UIStateException. This UIStateException will then introduce the next UIState.
     * @param tokens inputted parameters
     * @throws ResponseException errors if they come up. Otherwise,
     * @throws UIStateException as the success state, which will keep the scanner object open and seamlessly continue
     * to the next UI state.
     */
    private void login(String[] tokens) throws ResponseException {
        validateParameterLength(tokens, 3);
        String login = client.login(tokens[1], tokens[2]);
        throw new UIStateException(new PostloginUI(client), login);
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

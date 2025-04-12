package ui;

import client.ChessClient;
import exception.ResponseException;

public class PostloginUI extends BaseUI {

    public PostloginUI(ChessClient client) {
        super(client);
    }

    @Override
    public String handler(String input) throws ResponseException {
        String[] tokens = input.split(" ");
        return switch (tokens[0].toLowerCase()) {
            case "list" -> list();
            case "create" -> create(tokens);
            case "join" -> join(tokens);
            case "observe" -> observe(tokens);
            case "logout" -> logoutUser();
            default -> displayHelpInfo();
        };
    }

    private String list() throws ResponseException {
        return client.listGames();
    }

    private String create(String[] tokens) throws ResponseException {
        validateParameterLength(tokens, 2);
        String gameName = tokens[1];
        return client.createGame(gameName);
    }

    private String join(String[] tokens) throws ResponseException {
        validateParameterLength(tokens, 3);
        String joinTeam = tokens[1];
        String gameID = tokens[2];
        return client.joinGame(joinTeam, gameID);
    }

    private String observe(String[] tokens) throws ResponseException {
        validateParameterLength(tokens, 2);
        return client.observeGame(tokens[1]);
    }

    @Override
    public String displayHelpInfo() {
        return "";
    }
}

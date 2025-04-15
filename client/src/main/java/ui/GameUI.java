package ui;

import client.ChessClient;
import exception.ResponseException;

public class GameUI extends BaseUI {

    public GameUI(ChessClient client, ChessboardDrawer drawer) {
        super(client);
        state = UIStatesEnum.GAMEUI;
    }

    @Override
    public String handler(String input) throws ResponseException {
        String[] tokens = input.split(" ");
        return switch(tokens[0].toLowerCase()) {
            case "quit" -> quitGame();
            default -> displayHelpInfo();
        };
    }

    private String quitGame() throws ResponseException {
        int gameID = client.getDataCache().getCurrentGameID();
        String gameName = client.getDataCache().getGameByIndex(gameID).gameName();
//        client.quitGame();
        return "Left game " + gameName + "successfully.";
    }

    @Override
    public String displayHelpInfo() {
        return """
    --- GAME COMMANDS ---
    Type a command to get the corresponding action.
    - quit      | Leave your current game.
    - help      | Display this help menu.
    """;
    }
}

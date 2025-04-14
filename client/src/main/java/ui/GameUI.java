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
            default -> displayHelpInfo();
        };
    }

    @Override
    public String displayHelpInfo() {
        return "";
    }
}

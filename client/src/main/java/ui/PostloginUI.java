package ui;

import chess.ChessGame;
import client.ChessClient;
import exception.ResponseException;
import models.GameData;

public class PostloginUI extends BaseUI {

    public PostloginUI(ChessClient client) {
        super(client);
        state = UIStatesEnum.POSTLOGINUI;
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
        String result = client.joinGame(joinTeam,gameID);
        ChessGame.TeamColor teamColor = (joinTeam.toUpperCase() == "WHITE" ?
                ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK);

        GameData gameData = client.getDataCache().getGameByIndex(Integer.parseInt(gameID));
        ChessboardDrawer drawer = new ChessboardDrawer(gameData.game(), teamColor);
        GameUI gameUI = new GameUI(client, drawer);
        client.setState(gameUI);

        return result;
    }

    private String observe(String[] tokens) throws ResponseException {
        validateParameterLength(tokens, 2);
        int gameID = Integer.parseInt(tokens[1]);
        String result = client.observeGame(tokens[1]);

        GameData gameData = client.getDataCache().getGameByIndex(gameID);
        ChessboardDrawer drawer = new ChessboardDrawer(gameData.game(), ChessGame.TeamColor.WHITE);
        GameUI gameUI = new GameUI(client, drawer);
        client.setState(gameUI);

        return result;
    }

    private String logoutUser() throws ResponseException {
        return client.logout();
    }

    @Override
    public String displayHelpInfo() {
        return "";
    }
}

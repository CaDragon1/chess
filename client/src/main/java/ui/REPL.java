package ui;

import chess.ChessGame;
import client.ChessClient;
import exception.ResponseException;

import java.util.Scanner;

public class REPL {
    private String url;
    private final ChessClient client;
    private BaseUI currentUI;
    private final Scanner scanner;

    // Constructor to set up url, client object, scanner
    public REPL(String serverURL) {
        url = serverURL;
        client = new ChessClient(url, null);
        currentUI = new PreloginUI(client);
        scanner = new Scanner(System.in);
    }


    public void run() {
        System.out.println("Welcome to Minecraft, I am Steve");

        // Trim inputs for accuracy
        String input = scanner.nextLine().trim();

        try {
            String result = currentUI.handler(input);
            System.out.println(result);
        } catch (ResponseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Function to change UIState so that each loop seeks different commands
     */
    private void updateState() {
        UIStatesEnum currentState = client.getCurrentState();

        if (currentUI.state != currentState) {
            switch (currentState) {
                case PRELOGINUI -> currentUI = new PreloginUI(client);
                case POSTLOGINUI -> currentUI = new PostloginUI(client);
                case GAMEUI -> {
                    setGameUI();
                }
            }
        }
    }

    private void setGameUI() {
        // create new drawer to pass to currentUI
        int currentGameID = client.getDataCache().getCurrentGameID();
        ChessGame currentGame = client.getDataCache().getGameByIndex(currentGameID).game();
        ChessboardDrawer drawer = new ChessboardDrawer(currentGame, ChessGame.TeamColor.WHITE);
        currentUI = new GameUI(client, drawer);
    }
}

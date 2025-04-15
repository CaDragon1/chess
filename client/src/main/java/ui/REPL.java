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
        boolean keepRunning = true;
        System.out.println("Welcome to Minecraft, I am Steve\nType a command here! Type 'help' for a list of commands.");
        System.out.print(">");

        // Trim inputs for accuracy
        while (keepRunning) {
            String input = scanner.nextLine().trim();

            try {
                String result = currentUI.handler(input);
                // Adding extra commands for user friendliness :D
                if (result.equalsIgnoreCase("quit") || result.equalsIgnoreCase("exit") ||
                        result.equalsIgnoreCase("stop") || result.equalsIgnoreCase("back")) {
                    updateState();
                }
                System.out.println(result);
            } catch (ResponseException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    /**
     * Function to change UIState so that each loop seeks different commands
     */
    private void updateState() {

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

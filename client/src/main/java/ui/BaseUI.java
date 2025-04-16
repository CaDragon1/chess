package ui;

import client.ChessClient;
import exception.ResponseException;

import java.io.InputStream;
import java.util.Scanner;

public abstract class BaseUI implements UIState{
    protected final ChessClient client;
    public UIStatesEnum state;
    private Scanner scanner;

    public BaseUI(ChessClient client) {
        scanner = new Scanner(System.in);
        this.client = client;
    }

    public void validateParameterLength(String[] params, int expectedLength) throws ResponseException {
        if (params.length < expectedLength) {
            throw new ResponseException("Parameters missing", 400);
        }
        else if (params.length > expectedLength) {
            throw new ResponseException("Too many parameters given", 400);
        }
    }

    public void run() {
        boolean keepRunning = true;
        System.out.println("Welcome to Minecraft, I am Steve\nType a command here! Type 'help' for a list of commands.");
        System.out.print(">");

        // Trim inputs for accuracy
        while (keepRunning) {
            String input = scanner.nextLine().trim();

            try {
                String result = handler(input);
                // Adding extra commands for user friendliness :D
                if (result.equalsIgnoreCase("quit") || result.equalsIgnoreCase("exit") ||
                        result.equalsIgnoreCase("stop") || result.equalsIgnoreCase("back")) {
                    keepRunning = false;
                }
                System.out.println(result);
            } catch (ResponseException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }
}

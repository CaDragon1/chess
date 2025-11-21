package client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//import com.sun.nio.sctp.HandlerResult;
//import com.sun.nio.sctp.Notification;
//import com.sun.nio.sctp.NotificationHandler;
import exception.ResponseException;

import java.util.Scanner;

public class Repl {
    private Client client;
    private final ServerFacade server;
    private final PreLoginClient preClient;

    public Repl(String serverURL) {
        server = new ServerFacade(serverURL);
        preClient = new PreLoginClient(server);

        client = preClient;
    }
    public void run() {
        System.out.println("Welcome to the Chess client! Please register or log in.\nType 'help' for available commands.");

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equalsIgnoreCase("{\"message\":\"Quitting application...\"}")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                client = handleClientState(result);

                try {
                    System.out.println(extractMessage(result));
                } catch (Exception e) {
                    System.out.println(result);
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.println(msg);
            }
        }
        System.out.println("Goodbye!");
    }

    private void printPrompt() {
        System.out.print("> ");
    }

    private Client handleClientState(String evalResult) throws ResponseException {
        PostLoginClient postClient;
        GameClient gameClient;
        try {
            String evalMessage = extractMessage(evalResult);

            if (evalMessage.contains("successfully logged in") || evalMessage.contains("successfully registered")
                    || evalMessage.contains("Successfully exited game")) {
                String authToken = extractAuthToken(evalResult);

                postClient = new PostLoginClient(server, authToken);
                return postClient;
            } else if (evalMessage.contains("Successfully logged out")) {
                return preClient;
            } else if (evalMessage.contains("Joining game")) {
                String authToken = extractAuthToken(evalResult);
                int gameID = extractGameID(evalResult);
                String teamColor = extractTeamColor(evalResult);
                gameClient = new GameClient(server, authToken, gameID, teamColor);

                return gameClient;
            } else if (evalMessage.contains("Observing game")) {
                String authToken = extractAuthToken(evalResult);
                int gameID = extractGameID(evalResult);
                gameClient = new GameClient(server, authToken, gameID, null);

                return gameClient;
            }
        } catch (Exception e) {
            throw new ResponseException("Error: Json objects parsed incorrectly --> " + e.getMessage(), 500);
        }
        return client;
    }

    // Extract auth token from gson returns
    private String extractAuthToken(String evalResult) {
        JsonObject json = JsonParser.parseString(evalResult).getAsJsonObject();
        return json.get("authToken").getAsString();
    }

    private String extractMessage(String evalResult) {
        JsonObject json = JsonParser.parseString(evalResult).getAsJsonObject();
        return json.get("message").getAsString();
    }

    private int extractGameID(String evalResult) {
        JsonObject json = JsonParser.parseString(evalResult).getAsJsonObject();
        return json.get("gameID").getAsInt();
    }

    private String extractTeamColor(String evalResult) {
        JsonObject json = JsonParser.parseString(evalResult).getAsJsonObject();
        return json.get("teamColor").getAsString();
    }

//    @Override
//    public HandlerResult handleNotification(Notification notification, Object attachment) {
//        return null;
//    }
}

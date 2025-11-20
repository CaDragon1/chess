package client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.NotificationHandler;
import exception.ResponseException;

import java.util.Scanner;

public class Repl implements NotificationHandler {
    private Client client;
    private final ServerFacade server;
    private final PreLoginClient preClient;
    private PostLoginClient postClient;
    private GameClient gameClient;

    public Repl(String serverURL) {
        server = new ServerFacade(serverURL);
        preClient = new PreLoginClient(server);
        postClient = new PostLoginClient(server, null);
        gameClient = new GameClient(server, null, null);

        client = preClient;
    }
    public void run() {
        System.out.println("Welcome to the Chess client! Please register or log in.");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equalsIgnoreCase("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                client = handleClientState(result);

                System.out.println(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.println(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.println();
    }

    private Client handleClientState(String evalResult) throws ResponseException {
        if (evalResult.contains("successfully logged in") || evalResult.contains("successfully registered")
                || evalResult.contains("successfully exited game")) {
            String authToken = extractAuthToken(evalResult);

            postClient = new PostLoginClient(server, authToken);
            return postClient;
        }
        else if (evalResult.contains("successfully logged out")) {
            postClient = new PostLoginClient(server, null);
            gameClient = new GameClient(server, null, null);
            return preClient;
        }
        else if (evalResult.contains("joining game")) {
            String authToken = extractAuthToken(evalResult);
            int gameID = extractGameID(evalResult);
            String teamColor = extractTeamColor(evalResult);
            gameClient = new GameClient(server, authToken, gameID, teamColor);

            return gameClient;
        }
        else if (evalResult.contains("observing game")) {
            String authToken = extractAuthToken(evalResult);
            int gameID = extractGameID(evalResult);
            gameClient = new GameClient(server, authToken, gameID, null);

            return gameClient;
        }
        return client;
    }

    // Extract auth token from gson returns
    private String extractAuthToken(String evalResult) {
        JsonObject json = JsonParser.parseString(evalResult).getAsJsonObject();
        return json.get("authToken").getAsString();
    }

    private int extractGameID(String evalResult) {
        JsonObject json = JsonParser.parseString(evalResult).getAsJsonObject();
        return json.get("gameID").getAsInt();
    }

    private String extractTeamColor(String evalResult) {
        JsonObject json = JsonParser.parseString(evalResult).getAsJsonObject();
        return json.get("teamColor").getAsString();
    }

    @Override
    public HandlerResult handleNotification(Notification notification, Object attachment) {
        return null;
    }
}

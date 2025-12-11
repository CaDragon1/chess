package client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    /**
     * Function to run the REPL loop. Has an endstate.
     */
    public void run() {
        System.out.println("Welcome to the Chess client! Please register or log in.\nType 'help' for available commands.");

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equalsIgnoreCase("{\"message\":\"Quitting application...\"}")) {
            System.out.print("> ");
            String line = scanner.nextLine();
            try {
                result = client.eval(line);
                client = handleClientState(result);

                try {
                    System.out.println(extractMessage(result));
                } catch (Exception ignored) {}

            } catch (Throwable e) {
                var msg = e.toString();
                System.out.println(msg);
            }
        }
        System.out.println("Goodbye!");
    }

    /**
     * This function determines what the client state should be based on the evaluation result.
     * @param evalResult is the result from evaluating and running a command
     * @return postClient if the user logs in or leaves game, gameClient if the user joins a game, and preClient if logging out
     * @throws ResponseException if there are issues parsing json objects
     */
    private Client handleClientState(String evalResult) throws ResponseException {
        PostLoginClient postClient;
        GameClient gameClient;
        try {
            String evalMessage = extractMessage(evalResult);

            if (evalMessage.contains("successfully logged in") ||
                    evalMessage.contains("successfully registered") ||
                    evalMessage.contains("successfully exited game")) {
                String authToken = extractAuthToken(evalResult);
                postClient = new PostLoginClient(server, authToken);
                return postClient;
            }

            else if (evalMessage.contains("Successfully logged out")) {
                return preClient;
            }

            else if (evalMessage.contains("Joining game")) {
                String authToken = extractAuthToken(evalResult);
                int gameID = extractGameID(evalResult);
                String teamColor = extractTeamColor(evalResult);
                gameClient = new GameClient(server, authToken, gameID, teamColor);

                return gameClient;
            }

            else if (evalMessage.contains("Observing game")) {
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

    /**
     * Extract auth token from gson returns
     * @param evalResult is the result from evaluation
     * @return the authtoken
     */
    private String extractAuthToken(String evalResult) {
        JsonObject json = JsonParser.parseString(evalResult).getAsJsonObject();
        return json.get("authToken").getAsString();
    }

    /**
     * Extract the message from a gson return
     * @param evalResult is the result of evaluation of a command
     * @return the message
     */
    private String extractMessage(String evalResult) {
        JsonObject json = JsonParser.parseString(evalResult).getAsJsonObject();
        return json.get("message").getAsString();
    }

    /**
     * Extract the gameID from a gson return
     * @param evalResult is the result of evaluation of a command
     * @return the gameID
     */
    private int extractGameID(String evalResult) {
        JsonObject json = JsonParser.parseString(evalResult).getAsJsonObject();
        return json.get("gameID").getAsInt();
    }

    /**
     * Extract the team color from a gson return
     * @param evalResult is the result of evaluation of a command
     * @return the team color as string
     */
    private String extractTeamColor(String evalResult) {
        JsonObject json = JsonParser.parseString(evalResult).getAsJsonObject();
        return json.get("teamColor").getAsString();
    }
}

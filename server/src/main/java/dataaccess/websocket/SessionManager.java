package dataaccess.websocket;

import io.javalin.websocket.WsContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * SessionManager should make things easier for me when calling websocket things. For instance, it should manage
 * commands that update for each player in a given game session; it also needs to track all the clients and game sessions.
 */
public class SessionManager {
    // map of all active game sessions
    private final Map<Integer, Set<WsContext>> gameSessions = new HashMap<>();
    // and map of all clients
    private final Map<WsContext, ClientData> clients = new HashMap<>();

    // Make it synchronized so we don't have any weird multithreading issues
    public synchronized void addClient(int gameID, String username, WsContext ctx) {
        clients.put(ctx, new ClientData(gameID, username));
    }

    public synchronized void removeClient(WsContext ctx) {
        // Remove client from game and reset that game's username to null
    }

    public synchronized Set<WsContext> getCurrentClients(int gameID) {
        Set<WsContext> sessions = gameSessions.get(gameID);
        if (sessions == null) {
            return Set.of();
        }
        return sessions;
    }
}

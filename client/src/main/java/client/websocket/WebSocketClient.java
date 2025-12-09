package client.websocket;

import com.google.gson.Gson;
import jakarta.websocket.*;
import models.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.util.Scanner;

public class WebSocketClient extends Endpoint {
    private Session session;
    private final Gson gson = new Gson();
    private final GameMessageHandler gameMessageHandler;


    public static void main(String[] args) throws Exception {
        WebSocketClient client = new WebSocketClient();

        Scanner scanner = new Scanner(System.in);

        // I assume I need this to send the command to the serverside WebSocketHandler somehow
    }

    public WebSocketClient(URI uri, GameMessageHandler handler) throws Exception {
        gameMessageHandler = handler;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                handleMessage(message);
            }
        });
    }

    private void handleMessage(String json) {
        ServerMessage message = gson.fromJson(json, ServerMessage.class);
        switch (message.getServerMessageType()) {
            case NOTIFICATION -> gameMessageHandler.onNotification(message.getMessage());
            case LOAD_GAME -> gameMessageHandler.onLoadGame(message.getGame());
            case ERROR -> gameMessageHandler.onError(message.getErrorMessage());
        }
    }

    public void send(UserGameCommand command) throws IOException {
        String json = gson.toJson(command);
        session.getBasicRemote().sendText(json);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        System.out.println("Connected websocket");
    }

    @Override
    public void onClose(Session session, CloseReason reason) {
        System.out.println("Closed websocket: " + reason);
    }

    @Override
    public void onError(Session session, Throwable error) {
        gameMessageHandler.onError("WebSocket error: " + error.getMessage());
    }
}

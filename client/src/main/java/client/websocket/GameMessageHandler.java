package client.websocket;

import models.GameData;

public interface GameMessageHandler {
    void onLoadGame(GameData game);
    void onNotification(String message);
    void onError(String error);
}

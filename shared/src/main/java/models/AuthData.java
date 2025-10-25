package models;

import com.google.gson.Gson;

public record AuthData(String authToken, String username) {

    public String toString() {
        return new Gson().toJson(this);
    }
}
package com.game.model;

public class AuthResponse {
    private GameUser user;
    private String token;

    public GameUser getUser() {
        return user;
    }

    public void setUser(GameUser user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}


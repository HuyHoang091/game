package com.game.Model; // hoặc package phù hợp với bạn

import com.game.Model.User;

public class AuthResponse {
    private String token;
    private User user;

    public AuthResponse(User user, String token) {
        this.user = user;
        this.token = token;
    }

    // Getters và setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
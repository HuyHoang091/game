package com.game.model;

import lombok.Data;

@Data
public class GameUser {
    private Long id;
    
    private String username;
    private String password;
    private String email;
    private int tiendo;
    private String trangthai;
    private String sessionId;
}
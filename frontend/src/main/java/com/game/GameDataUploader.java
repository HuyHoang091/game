package com.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.data.GameData;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpResponse;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameDataUploader {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void startAutoUpload() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // 1. Gửi character
                if (GameData.character != null && !GameData.character.isEmpty())
                    sendJson("http://localhost:8080/api/characters/batch", GameData.character);

                // 2. Gửi characterSkills
                if (GameData.characterSkills != null && !GameData.characterSkills.isEmpty())
                    sendJson("http://localhost:8080/api/character_skills/batch", GameData.characterSkills);

                // 3. Gửi inventory
                if (GameData.inventory != null && !GameData.inventory.isEmpty())
                    sendJson("http://localhost:8080/api/inventory/batch", GameData.inventory);

                // 4. Gửi itemInstance
                if (!GameData.itemInstance.isEmpty())
                    sendJson("http://localhost:8080/api/iteminstance/batch", GameData.itemInstance);

                if (GameData.user != null)
                    sendJson("http://localhost:8080/api/users/" + GameData.user.getId(), GameData.user);
                
            } catch (Exception e) {
                System.err.println("Error uploading game data: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS); // Gửi mỗi 5 giây
    }

    public static void stopAutoUpload() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
            System.out.println("Auto upload stopped.");
        }
    }

    private static void sendJson(String url, Object data) throws Exception {
        String json = objectMapper.writeValueAsString(data);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut put = new HttpPut(url);

            put.setHeader("Content-Type", "application/json");
            put.setHeader("Authorization", "Bearer " + GameData.token);
            put.setEntity(new StringEntity(json, "UTF-8"));

            HttpResponse response = client.execute(put);
            int statusCode = response.getStatusLine().getStatusCode();

            System.out.println("Sent to " + url + " | Status: " + statusCode);
        }
    }
}
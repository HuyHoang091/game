package com.game.core;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class KeyBindingConfig {
    private static final String FILE_NAME = "keybindings.properties";
    private static Map<String, Integer> keyBindings = new HashMap<>();

    static {
        keyBindings.put("Up", KeyEvent.VK_W);
        keyBindings.put("Down", KeyEvent.VK_S);
        keyBindings.put("Left", KeyEvent.VK_A);
        keyBindings.put("Right", KeyEvent.VK_D);
        keyBindings.put("Speed", KeyEvent.VK_SHIFT);
        keyBindings.put("Interact", KeyEvent.VK_F);
        keyBindings.put("Attack", KeyEvent.VK_SPACE);
        keyBindings.put("Skill1", KeyEvent.VK_Q);
        keyBindings.put("Skill2", KeyEvent.VK_E);
        keyBindings.put("Skill3", KeyEvent.VK_R);
        keyBindings.put("Skill4", KeyEvent.VK_T);
        keyBindings.put("Dodge", KeyEvent.VK_G);
        keyBindings.put("Open Skill Tree", KeyEvent.VK_K);
        keyBindings.put("Open Inventory", KeyEvent.VK_B);
        keyBindings.put("Open Setting", KeyEvent.VK_ESCAPE);
        loadBindings();
    }

    public static Map<String, Integer> getAllBindings() {
        return keyBindings;
    }

    public static int getKey(String action) {
        return keyBindings.getOrDefault(action, KeyEvent.VK_UNDEFINED);
    }

    public static void setKey(String action, int keyCode) {
        // Không cho trùng key
        if (keyBindings.containsValue(keyCode)) return;
        keyBindings.put(action, keyCode);
    }

    public static void saveBindings() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Map.Entry<String, Integer> entry : keyBindings.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadBindings() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            Map<String, Integer> loaded = new LinkedHashMap<>();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String action = parts[0].trim();
                    int key = Integer.parseInt(parts[1].trim());
                    loaded.put(action, key);
                }
            }
            if (!loaded.isEmpty()) keyBindings = loaded;
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}

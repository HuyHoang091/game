package com.game;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class HashClient {
    public static Socket socket;

    public static void main(String[] args) throws Exception {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        AtomicBoolean isVerified = new AtomicBoolean(false);

        IO.Options opts = new IO.Options();
        socket = IO.socket("http://localhost:9092", opts);

        socket.on(Socket.EVENT_CONNECT_ERROR, args1 -> {
            JOptionPane.showMessageDialog(null,
                "Không thể kết nối đến máy chủ.\nVui lòng kiểm tra kết nối mạng.",
                "Lỗi kết nối",
                JOptionPane.ERROR_MESSAGE);
            AccessFrame.getInstance().setMs(999);
        });

        socket.on(Socket.EVENT_CONNECT_TIMEOUT, args1 -> {
            JOptionPane.showMessageDialog(null,
                "Kết nối đến máy chủ bị gián đoạn.\nVui lòng kiểm tra lại kết nối.",
                "Timeout kết nối",
                JOptionPane.ERROR_MESSAGE);
            AccessFrame.getInstance().setMs(423);
        });

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    File directory = new File("target/classes");
                    String hash = hashDirectory(directory);
                    socket.emit("send_hash", hash);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on("verify_result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String result = (String) args[0];
                System.out.println("Server verify result: " + result);
                AccessFrame.getInstance().setMs(16);
                if (result.startsWith("OK|")) {
                    String sessionCode = result.substring(3); // sau "OK|"
                    
                    AccessFrame.getInstance().frontendSecret = sessionCode;
                    isVerified.set(true);
                } else {
                    String response = result.substring(7);
                    JOptionPane.showMessageDialog(null, response, "Cảnh báo", JOptionPane.ERROR_MESSAGE);
                    socket.disconnect();
                    System.exit(0);
                }
            }
        });

        socket.on("pong_result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    long serverTime = Long.parseLong((String) args[0]);
                    long now = System.currentTimeMillis();
                    int latency = (int) (now - serverTime);
                    SwingUtilities.invokeLater(() -> {
                        AccessFrame.getInstance().setMs(latency);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        scheduler.scheduleAtFixedRate(() -> {
            if (!isVerified.get()) return;
            socket.emit("ping_custom", "check"); 
        }, 0, 1, TimeUnit.SECONDS);

        socket.connect();
    }

    public static void checkHash() {
        try {
            File directory = new File("target/classes");
            String hash = hashDirectory(directory);
            socket.emit("send_hash", hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String hashDirectory(File directory) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        java.util.List<File> files = Files.walk(directory.toPath())
                .filter(Files::isRegularFile)
                .sorted()
                .map(Path::toFile)
                .collect(Collectors.toList());

        for (File file : files) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            digest.update(bytes);
        }

        byte[] hash = digest.digest();
        return Base64.getEncoder().encodeToString(hash);
    }
}
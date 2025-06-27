package com.game.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.game.Model.AppCodeParts;

@Service
public class AppCodeService {
    private final Map<String, AppCodeParts> appCodeStorage = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(200);

    @Autowired
    private UserService userService;

    @Async("appTaskExecutor")
    public void startAppCodeTimeout(String username) {
        scheduler.schedule(() -> {
            AppCodeParts parts = appCodeStorage.get(username);
            if (parts != null && parts.getPart2() == null) {
                userService.logout(username);
                appCodeStorage.remove(username);
            }
        }, 12, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
            AppCodeParts parts = appCodeStorage.get(username);
            if (parts != null && parts.getPart3() == null) {
                userService.logout(username);
                appCodeStorage.remove(username);
            }
        }, 24, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
            AppCodeParts parts = appCodeStorage.get(username);
            if (parts != null) {
                if (parts.isSecret()) {
                    appCodeStorage.remove(username);
                } else {
                    userService.logout(username);
                    appCodeStorage.remove(username);
                }
            }
        }, 30, TimeUnit.SECONDS);
    }

    public void initAppCode(String username, String part1) {
        AppCodeParts parts = new AppCodeParts();
        parts.setPart1(part1);
        parts.setTimestamp(System.currentTimeMillis());
        parts.setSecret(false);
        appCodeStorage.put(username, parts);
    }

    public boolean AppCode2(String username, String part2) {
        AppCodeParts parts = appCodeStorage.get(username);
        if ((System.currentTimeMillis() - parts.getTimestamp()) >= 5+000 && (System.currentTimeMillis() - parts.getTimestamp()) <= 10_000) {
            parts.setPart2(part2);
            parts.setTimestamp(System.currentTimeMillis());
            return true;
        } else {
            return false;
        }
    }

    public boolean AppCode3(String username, String part3, String APP_CODE) {
        AppCodeParts parts = appCodeStorage.get(username);
        if ((System.currentTimeMillis() - parts.getTimestamp()) >= 5_000 && (System.currentTimeMillis() - parts.getTimestamp()) <= 10_000) {
            parts.setPart3(part3);
            String fullAppCode = parts.getPart1() + parts.getPart2() + parts.getPart3();
            if (fullAppCode.equals(APP_CODE)) {
                parts.setSecret(true);
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    public void Fail(String username) {
        userService.logout(username);
        appCodeStorage.remove(username);
    }

    public String generateOneTimeEncryptedPart(String username) {
        String part3 = "Hoang";
        long timestamp = System.currentTimeMillis();

        String content = String.format(
            "app.part3=%s\ncode.username=%s\ncode.time=%d",
            part3, username, timestamp
        );

        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }
}
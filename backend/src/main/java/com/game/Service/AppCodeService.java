package com.game.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.game.Model.AppCodeParts;
import com.game.Model.User;
import com.game.Repository.UserRepository;

@Service
public class AppCodeService {
    public final Map<String, AppCodeParts> appCodeStorage = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(200);
    private static final Logger logger = LoggerFactory.getLogger(AppCodeService.class);

    @Value("${hash.code}")
    private String validHashes;

    private final Map<String, Long> sessionStore = new ConcurrentHashMap<>();

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Value("${key.secret}")
    private String KEY_SECRET;

    @Async("appTaskExecutor")
    public void startAppCodeTimeout(String username) {
        scheduler.schedule(() -> {
            AppCodeParts parts = appCodeStorage.get(username);
            User user = userRepository.findByUsername(username);
            if (parts != null && parts.getPart2() == null && (user == null || "".equals(user.getSessionId()))) {
                userService.logout(username);
                appCodeStorage.remove(username);
            }
        }, 12, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
            AppCodeParts parts = appCodeStorage.get(username);
            User user = userRepository.findByUsername(username);
            if (parts != null && parts.getPart2() == null && (user == null || "".equals(user.getSessionId()))) {
                userService.logout(username);
                appCodeStorage.remove(username);
            }
        }, 24, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
            AppCodeParts parts = appCodeStorage.get(username);
            User user = userRepository.findByUsername(username);
            if (parts != null) {
                if (parts.isSecret()) {
                    appCodeStorage.remove(username);
                } else if (user == null || "".equals(user.getSessionId())) {
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
        logger.error("Người dùng [{}] có hành vi bất thường (Không xác thực được frontend!)", username);
    }

    public String generateOneTimeEncryptedPart(String username) throws Exception {
        String part3 = "Hoang";
        long timestamp = System.currentTimeMillis();

        String content = String.format(
            "app.part3=%s\ncode.username=%s\ncode.time=%d",
            part3, username, timestamp
        );

        String encrypted = encryptAppCodePart(content);

        return Base64.getEncoder().encodeToString(encrypted.getBytes(StandardCharsets.UTF_8));
    }

    public String encryptAppCodePart(String content) throws Exception {
        SecretKeySpec key = new SecretKeySpec(KEY_SECRET.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decryptAppCodePart(String encryptedBase64) throws Exception {
        SecretKeySpec key = new SecretKeySpec(KEY_SECRET.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public String verifyAppHash(String hash) {
        if (validHashes.contains(hash)) {
            String sessionCode = UUID.randomUUID().toString();
            sessionStore.put(sessionCode, System.currentTimeMillis());
            return sessionCode;
        }
        return null;
    }

    public boolean validateSessionCode(String sessionCode) {
        Long timestamp = sessionStore.get(sessionCode);
        if (timestamp == null || System.currentTimeMillis() - timestamp > 60_000) {
            return false;
        }
        return true;
    }

    public void invalidateSessionCode(String sessionCode) {
        sessionStore.remove(sessionCode);
    }
}
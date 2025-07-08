package com.game.Controllers;

import com.game.Model.User;
import com.game.Service.AppCodeService;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appcode")
public class AppCodeController {

    @Autowired
    private AppCodeService appCodeService;

    @Value("${app.code}")
    private String APP_CODE;

    @PostMapping("/part2")
    public ResponseEntity<?> receivePart2(@RequestBody User user, @RequestHeader("App-Code") String part2) {
        boolean no2 = appCodeService.AppCode2(user.getUsername(), part2);
        if (!no2) {
            appCodeService.Fail(user.getUsername());
            return ResponseEntity.status(408).body(null);
        }

        return ResponseEntity.ok(null);
    }

    @PostMapping("/part3")
    public ResponseEntity<?> receivePart3(@RequestBody User user, @RequestHeader("App-Code") String encoded) {
        try {
            String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            String textReal = appCodeService.decryptAppCodePart(decoded);

            Properties props = new Properties();
            props.load(new StringReader(textReal));

            String part3 = props.getProperty("app.part3");
            String username = props.getProperty("code.username");
            long time = Long.parseLong(props.getProperty("code.time"));

            if (!username.equals(user.getUsername())) {
                return ResponseEntity.status(408).body(null);
            }

            long now = System.currentTimeMillis();
            if (now - time > 30_000) {
                appCodeService.Fail(user.getUsername());
                return ResponseEntity.status(408).body(null);
            }

            boolean no3 = appCodeService.AppCode3(user.getUsername(), part3, APP_CODE);
            if (!no3) {
                appCodeService.Fail(user.getUsername());
                return ResponseEntity.status(408).body(null);
            }

            return ResponseEntity.ok(null);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Lỗi giải mã app-code: " + e.getMessage());
        }
    }

    @PostMapping("/download")
    public ResponseEntity<Resource> downloadAppCode(@RequestBody User user) {
        try {
            String part = appCodeService.generateOneTimeEncryptedPart(user.getUsername());

            Path path = Files.createTempFile("app_part_", ".dat");
            Files.writeString(path, part, StandardCharsets.UTF_8);

            Resource resource = new UrlResource(path.toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path.getFileName().toString())
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}

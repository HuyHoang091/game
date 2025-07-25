package com.game.Controllers;

import com.game.Model.Map;
import com.game.Service.CustomUserDetails;
import com.game.Service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/map")
public class MapController {

    @Autowired
    private MapService mapService;

    @Value("${admin.secret}")
    private String SECRET_CODE;

    @GetMapping("/")
    public ResponseEntity<List<Map>> getAllMap(@RequestHeader(value = "Xac-thuc", required = false) String adminHeader, Authentication authentication) {
        
        if (adminHeader != null) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            if (!adminHeader.equals(SECRET_CODE) || !currentUser.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(403).build();
            }
        }

        List<Map> characters = mapService.getAllMap();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/")
    public ResponseEntity<?> createMap(@RequestBody Map Map) {
        Map create = mapService.createMap(Map);
        if (create != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Thêm mới thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMap(@PathVariable Long id, @RequestBody Map Map) {
        Map update = mapService.updateMap(id, Map);
        if (update != null) {
            return ResponseEntity.ok("Cập nhật thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMap(@PathVariable Long id) {
        boolean delete = mapService.deleteMap(id);
        if (delete) {
            return ResponseEntity.ok("Xóa thành công!");
        }
        return ResponseEntity.notFound().build();
    }
}
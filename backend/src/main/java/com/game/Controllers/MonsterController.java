package com.game.Controllers;

import com.game.Model.Monster;
import com.game.Service.CustomUserDetails;
import com.game.Service.MonsterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/monster")
public class MonsterController {

    @Autowired
    private MonsterService characterService;

    @Value("${admin.secret}")
    private String SECRET_CODE;

    @GetMapping("/")
    public ResponseEntity<List<Monster>> getAllMonster(@RequestHeader(value = "Xac-thuc", required = false) String adminHeader, Authentication authentication) {
        
        if (adminHeader != null) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            if (!adminHeader.equals(SECRET_CODE) || !currentUser.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(403).build();
            }
        }
        
        List<Monster> characters = characterService.getAllMonster();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/")
    public ResponseEntity<?> createMonster(@RequestBody Monster Monster) {
        Monster create = characterService.createMonster(Monster);
        if (create != null) {
            return ResponseEntity.ok("Thêm mới thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMonster(@PathVariable Long id, @RequestBody Monster Monster) {
        Monster update = characterService.updateMonster(id, Monster);
        if (update != null) {
            return ResponseEntity.ok("Cập nhật thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMonster(@PathVariable Long id) {
        boolean delete = characterService.deleteMonster(id);
        if (delete) {
            return ResponseEntity.ok("Xóa thành công!");
        }
        return ResponseEntity.notFound().build();
    }
}
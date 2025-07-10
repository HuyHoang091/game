package com.game.Controllers;

import com.game.Model.MonsterDrop;
import com.game.Service.CustomUserDetails;
import com.game.Service.MonsterDropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/monsterdrop")
public class MonsterDropController {

    @Autowired
    private MonsterDropService monsterDropService;

    @Value("${admin.secret}")
    private String SECRET_CODE;

    @GetMapping("/")
    public ResponseEntity<List<MonsterDrop>> getAllMonsterDrop(@RequestHeader(value = "Xac-thuc", required = false) String adminHeader, Authentication authentication) {
        
        if (adminHeader != null) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            if (!adminHeader.equals(SECRET_CODE) || !currentUser.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(403).build();
            }
        }
        
        List<MonsterDrop> characters = monsterDropService.getAllMonsterDrop();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/")
    public ResponseEntity<?> createMonsterDrop(@RequestBody MonsterDrop MonsterDrop) {
        MonsterDrop create = monsterDropService.createMonsterDrop(MonsterDrop);
        if (create != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Thêm mới thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMonsterDrop(@PathVariable Long id, @RequestBody MonsterDrop MonsterDrop) {
        MonsterDrop update = monsterDropService.updateMonsterDrop(id, MonsterDrop);
        if (update != null) {
            return ResponseEntity.ok("Cập nhật thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMonsterDrop(@PathVariable Long id) {
        boolean delete = monsterDropService.deleteMonsterDrop(id);
        if (delete) {
            return ResponseEntity.ok("Xóa thành công!");
        }
        return ResponseEntity.notFound().build();
    }
}
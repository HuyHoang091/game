package com.game.Controllers;

import com.game.Model.Character;
import com.game.Service.CharacterService;
import com.game.Service.CustomUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    @Autowired
    private CharacterService characterService;

    @GetMapping("/{user_id}")
    public ResponseEntity<List<Character>> getUserById(@PathVariable Long user_id, Authentication authentication) {
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        if (!currentUser.getId().equals(user_id) && !currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        List<Character> characters = characterService.getCharacters(user_id);
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<Character>> getAllCharacter() {
        List<Character> characters = characterService.getAllCharacter();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/")
    public ResponseEntity<?> createCharacter(@RequestBody Character character, Authentication authentication) {
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        if (!currentUser.getId().equals(character.getUserId()) && !currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        if (character.getUserId() == null || character.getName() == null || character.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Không được để trống Mã người dùng và Tên nhân vật!");
        }

        Character created = characterService.createCharacter(character);
        if (created != null) {
            return ResponseEntity.ok("Thêm mới thành công!");
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCharacter(@PathVariable Long id, @RequestBody Character character, Authentication authentication) {
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        if (!currentUser.getId().equals(character.getUserId()) && !currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        Character updated = characterService.updateCharacter(id, character);
        if (updated != null) {
            return ResponseEntity.ok("Cập nhật thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCharacter(@PathVariable Long id) {
        boolean deleted = characterService.deleteCharacter(id);
        if (deleted) {
            return ResponseEntity.ok("Xóa thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/batch")
    public ResponseEntity<Void> updateCharacters(@RequestBody List<Character> characters, Authentication authentication) {
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

        boolean isAdmin = currentUser.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        for (Character character : characters) {
            Long ownerId = character.getUserId();
            if (!isAdmin && !ownerId.equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }
        }

        characterService.updateListCharacters(characters);
        return ResponseEntity.ok().build();
    }
}
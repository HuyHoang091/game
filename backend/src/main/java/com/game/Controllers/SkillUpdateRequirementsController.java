package com.game.Controllers;

import com.game.Model.CustomUserDetails;
import com.game.Model.SkillUpdateRequirements;
import com.game.Service.SkillUpdateRequirementsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/skillupdate")
public class SkillUpdateRequirementsController {

    @Autowired
    private SkillUpdateRequirementsService characterService;

    @Value("${admin.secret}")
    private String SECRET_CODE;

    @GetMapping("/")
    public ResponseEntity<List<SkillUpdateRequirements>> getAllSkillUpdateRequirements(@RequestHeader(value = "Xac-thuc", required = false) String adminHeader, Authentication authentication) {
        
        if (adminHeader != null) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            if (!adminHeader.equals(SECRET_CODE) || !currentUser.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(403).build();
            }
        }
        
        List<SkillUpdateRequirements> characters = characterService.getAllSkillUpdateRequirements();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/")
    public ResponseEntity<SkillUpdateRequirements> createSkillUpdateRequirements(@RequestBody SkillUpdateRequirements SkillUpdateRequirements) {
        SkillUpdateRequirements create = characterService.createSkillUpdateRequirements(SkillUpdateRequirements);
        if (create != null) {
            return ResponseEntity.ok(create);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SkillUpdateRequirements> updateSkillUpdateRequirements(@PathVariable Long id, @RequestBody SkillUpdateRequirements SkillUpdateRequirements) {
        SkillUpdateRequirements update = characterService.updateSkillUpdateRequirements(id, SkillUpdateRequirements);
        if (update != null) {
            return ResponseEntity.ok(update);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SkillUpdateRequirements> deleteSkillUpdateRequirements(@PathVariable Long id) {
        boolean delete = characterService.deleteSkillUpdateRequirements(id);
        if (delete) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
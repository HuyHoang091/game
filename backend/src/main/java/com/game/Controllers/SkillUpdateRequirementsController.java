package com.game.Controllers;

import com.game.Model.SkillUpdateRequirements;
import com.game.Service.CustomUserDetails;
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
    private SkillUpdateRequirementsService skillUpdateRequirementsService;

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
        
        List<SkillUpdateRequirements> characters = skillUpdateRequirementsService.getAllSkillUpdateRequirements();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/")
    public ResponseEntity<?> createSkillUpdateRequirements(@RequestBody SkillUpdateRequirements SkillUpdateRequirements) {
        SkillUpdateRequirements create = skillUpdateRequirementsService.createSkillUpdateRequirements(SkillUpdateRequirements);
        if (create != null) {
            return ResponseEntity.ok("Thêm mới thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSkillUpdateRequirements(@PathVariable Long id, @RequestBody SkillUpdateRequirements SkillUpdateRequirements) {
        SkillUpdateRequirements update = skillUpdateRequirementsService.updateSkillUpdateRequirements(id, SkillUpdateRequirements);
        if (update != null) {
            return ResponseEntity.ok("Cập nhật thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSkillUpdateRequirements(@PathVariable Long id) {
        boolean delete = skillUpdateRequirementsService.deleteSkillUpdateRequirements(id);
        if (delete) {
            return ResponseEntity.ok("Xóa thành công!");
        }
        return ResponseEntity.notFound().build();
    }
}
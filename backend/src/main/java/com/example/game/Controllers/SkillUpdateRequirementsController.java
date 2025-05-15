package com.game.controller;

import com.game.entity.SkillUpdateRequirements;
import com.game.service.SkillUpdateRequirementsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/skillupdate")
public class SkillUpdateRequirementsController {

    @Autowired
    private SkillUpdateRequirementsService characterService;

    @GetMapping("/")
    public ResponseEntity<List<SkillUpdateRequirements>> getAllSkillUpdateRequirements() {
        List<SkillUpdateRequirements> characters = characterService.getAllSkillUpdateRequirements();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }
}
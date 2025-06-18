package com.game.Controllers;

import com.game.Model.Skill;
import com.game.Service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/skill")
public class SkillController {

    @Autowired
    private SkillService characterService;

    @GetMapping("/")
    public ResponseEntity<List<Skill>> getAllSkill() {
        List<Skill> characters = characterService.getAllSkill();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }
}
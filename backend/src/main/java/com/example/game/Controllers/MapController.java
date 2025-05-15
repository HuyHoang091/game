package com.game.controller;

import com.game.entity.Map;
import com.game.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/map")
public class MapController {

    @Autowired
    private MapService characterService;

    @GetMapping("/")
    public ResponseEntity<List<Map>> getAllMap() {
        List<Map> characters = characterService.getAllMap();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }
}
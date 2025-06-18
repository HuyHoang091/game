package com.game.Controllers;

import com.game.Model.Map;
import com.game.Service.MapService;
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

    @PostMapping("/")
    public ResponseEntity<Map> createMap(@RequestBody Map Map) {
        Map create = characterService.createMap(Map);
        if (create != null) {
            return ResponseEntity.ok(create);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map> updateMap(@PathVariable Long id, @RequestBody Map Map) {
        Map update = characterService.updateMap(id, Map);
        if (update != null) {
            return ResponseEntity.ok(update);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map> deleteMap(@PathVariable Long id) {
        boolean delete = characterService.deleteMap(id);
        if (delete) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
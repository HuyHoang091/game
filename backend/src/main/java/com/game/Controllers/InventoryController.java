package com.game.Controllers;

import com.game.Model.Inventory;
import com.game.Service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService characterService;

    @GetMapping("/{character_id}")
    public ResponseEntity<List<Inventory>> getAllCharactersSkill(@PathVariable Long character_id) {
        List<Inventory> characters = characterService.getAllCharactersSkill(character_id);
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<Inventory>> getAllInventory() {
        List<Inventory> characters = characterService.getAllInventory();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        Inventory create = characterService.createInventory(inventory);
        return ResponseEntity.ok(create);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody Inventory inventory) {
        Inventory update = characterService.updateInventory(id, inventory);
        if (update != null) {
            return ResponseEntity.ok(update);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Inventory> deleteInventory(@PathVariable Long id) {
        boolean delete = characterService.deleteInventory(id);
        if (delete) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/batch")
    public ResponseEntity<Void> updateInventorys(@RequestBody List<Inventory> characters) {
        characterService.updateListInventory(characters);
        return ResponseEntity.ok().build();
    }
}
package com.game.Controllers;

import com.game.Model.Inventory;
import com.game.Service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PreAuthorize("#user_id == principal.id or hasRole('ADMIN')")
    @GetMapping("/{user_id}/{character_id}")
    public ResponseEntity<List<Inventory>> getAllInventoryCharacter(@PathVariable Long user_id, @PathVariable Long character_id) {
        List<Inventory> characters = inventoryService.getAllInventoryCharacter(character_id);
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<Inventory>> getAllInventory() {
        List<Inventory> characters = inventoryService.getAllInventory();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<?> createInventory(@RequestBody Inventory inventory) {
        Inventory create = inventoryService.createInventory(inventory);
        if (create != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Thêm mới thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInventory(@PathVariable Long id, @RequestBody Inventory inventory) {
        Inventory update = inventoryService.updateInventory(id, inventory);
        if (update != null) {
            return ResponseEntity.ok("Cập nhật thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInventory(@PathVariable Long id) {
        boolean delete = inventoryService.deleteInventory(id);
        if (delete) {
            return ResponseEntity.ok("Xóa thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/batch")
    public ResponseEntity<Void> updateInventorys(@RequestBody List<Inventory> characters) {
        inventoryService.updateListInventory(characters);
        return ResponseEntity.ok().build();
    }
}
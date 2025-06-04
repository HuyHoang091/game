package com.game.controller;

import com.game.entity.Item;
import com.game.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/item")
public class ItemController {

    @Autowired
    private ItemService characterService;

    @GetMapping("/")
    public ResponseEntity<List<Item>> getAllItem() {
        List<Item> characters = characterService.getAllItem();
        return ResponseEntity.ok(characters);
    }

    @PostMapping("/")
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        Item create = characterService.createItem(item);
        if (create != null) {
            return ResponseEntity.ok(create);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        Item update = characterService.updateItem(id, item);
        if (update != null) {
            return ResponseEntity.ok(update);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Item> deleteItem(@PathVariable Long id) {
        boolean delete = characterService.deleteItem(id);
        if (delete) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
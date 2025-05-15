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
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }
}
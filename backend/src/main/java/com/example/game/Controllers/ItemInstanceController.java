package com.game.controller;

import com.game.entity.ItemInstance;
import com.game.service.ItemInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/iteminstance")
public class ItemInstanceController {

    @Autowired
    private ItemInstanceService characterService;

    @GetMapping("/")
    public ResponseEntity<List<ItemInstance>> getAllItemInstance() {
        List<ItemInstance> characters = characterService.getAllItemInstance();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }
}
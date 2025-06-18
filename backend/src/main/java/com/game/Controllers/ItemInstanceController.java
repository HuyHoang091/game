package com.game.Controllers;

import com.game.Model.ItemInstance;
import com.game.Service.ItemInstanceService;
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

    @PostMapping("/")
    public ResponseEntity<ItemInstance> createItemInstance(@RequestBody ItemInstance iteminstance) {
        ItemInstance create = characterService.createItemInstance(iteminstance);
        return ResponseEntity.ok(create);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemInstance> updateItemInstance(@PathVariable Long id, @RequestBody ItemInstance iteminstance) {
        ItemInstance update = characterService.updateItemInstance(id, iteminstance);
        if (update != null) {
            return ResponseEntity.ok(update);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ItemInstance> delete(@PathVariable Long id) {
        boolean delete = characterService.deleteItemInstance(id);
        if (delete) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/batch")
    public ResponseEntity<Void> updateItemInstances(@RequestBody List<ItemInstance> characters) {
        characterService.updateListItemInstance(characters);
        return ResponseEntity.ok().build();
    }
}
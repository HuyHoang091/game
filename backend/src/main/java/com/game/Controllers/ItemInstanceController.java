package com.game.Controllers;

import com.game.Model.ItemInstance;
import com.game.Service.CustomUserDetails;
import com.game.Service.ItemInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/iteminstance")
public class ItemInstanceController {

    @Autowired
    private ItemInstanceService characterService;

    @Value("${admin.secret}")
    private String SECRET_CODE;

    @GetMapping("/")
    public ResponseEntity<List<ItemInstance>> getAllItemInstance(@RequestHeader(value = "Xac-thuc", required = false) String adminHeader, Authentication authentication) {
        
        if (adminHeader != null) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            if (!adminHeader.equals(SECRET_CODE) || !currentUser.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(403).build();
            }
        }
        
        List<ItemInstance> characters = characterService.getAllItemInstance();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/")
    public ResponseEntity<?> createItemInstance(@RequestBody ItemInstance iteminstance) {
        ItemInstance create = characterService.createItemInstance(iteminstance);
        if (create != null) {
            return ResponseEntity.ok("Thêm mới thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItemInstance(@PathVariable Long id, @RequestBody ItemInstance iteminstance) {
        ItemInstance update = characterService.updateItemInstance(id, iteminstance);
        if (update != null) {
            return ResponseEntity.ok("Cập nhật thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        boolean delete = characterService.deleteItemInstance(id);
        if (delete) {
            return ResponseEntity.ok("Xóa thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/batch")
    public ResponseEntity<Void> updateItemInstances(@RequestBody List<ItemInstance> characters) {
        for (ItemInstance item : characters) {
            if (!characterService.isValidItemInstance(item)) {
                System.out.println("Item bất thường ID: " + item.getId());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        characterService.updateListItemInstance(characters);
        return ResponseEntity.ok().build();
    }
}
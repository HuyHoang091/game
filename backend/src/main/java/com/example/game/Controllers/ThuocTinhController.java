package com.game.controller;

import com.game.entity.ThuocTinh;
import com.game.service.ThuocTinhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/thuoctinh")
public class ThuocTinhController {

    @Autowired
    private ThuocTinhService characterService;

    @GetMapping("/")
    public ResponseEntity<List<ThuocTinh>> getAllThuocTinh() {
        List<ThuocTinh> characters = characterService.getAllThuocTinh();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }
}
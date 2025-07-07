package com.game.Controllers;

import com.game.Model.ThuocTinh;
import com.game.Service.ThuocTinhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/thuoctinh")
public class ThuocTinhController {

    @Autowired
    private ThuocTinhService thuocTinhService;

    @GetMapping("/")
    public ResponseEntity<List<ThuocTinh>> getAllThuocTinh() {
        List<ThuocTinh> characters = thuocTinhService.getAllThuocTinh();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }
}
package com.game.Controllers;

import com.game.Model.User;
import com.game.Service.UserService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.badRequest().body("Invalid credentials");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty() || user.getEmail() == null || user.getEmail().isEmpty()) {
            return new ResponseEntity<>("Không được bỏ trống Username, Email.", HttpStatus.BAD_REQUEST);
        }

        User registeredUser = userService.registerUser(user);
        if (registeredUser != null) {
            return new ResponseEntity<>("User registered successfully. Password sent to your email.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Đăng ký không thành công. Tên người dùng hoặc email có thể đã tồn tại.", HttpStatus.CONFLICT);
        }
    }

    // Truy vấn người dùng theo id
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> getAllUser() {
        List<User> user = userService.getAllUser();
        if (user != null && !user.isEmpty()) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/")
    public ResponseEntity<?> createUser(@RequestBody User character) {
        User created = userService.createUser(character);
        if (created != null) {
            return ResponseEntity.ok(created);
        }
        return ResponseEntity
            .badRequest()
            .body("Tài khoản hoặc email đã tồn tại, vui lòng chọn tên khác.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User character) {
        User updated = userService.updateUser(id, character);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
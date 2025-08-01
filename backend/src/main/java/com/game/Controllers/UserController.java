package com.game.Controllers;

import com.game.Model.User;
import com.game.Service.UserService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Value("${app.code}")
    private String APP_CODE;

    @Value("${app.part1}")
    private String PART1;

     @Value("${app.repass}")
    private String REPASS;

    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<User>> getAllUser() {
        List<User> user = userService.getAllUser();
        if (user != null && !user.isEmpty()) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<?> createUser(@RequestBody User character) {
        if (character.getUsername() == null || character.getUsername().trim().isEmpty()
            || character.getEmail() == null || character.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Không được để trống Username và Email!");
        }
        User created = userService.createUser(character);
        if (created != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Thêm mới thành công!");
        }
        return ResponseEntity
            .badRequest()
            .body("Tài khoản hoặc email đã tồn tại, vui lòng chọn tên khác.");
    }

    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User character) {
        User updated = userService.updateUser(id, character);
        if (updated != null) {
            return ResponseEntity.ok("Cập nhật thành công!");
        }
        return ResponseEntity.badRequest().body("Không được phép thay đổi tên ADMIN!");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok("Xóa thành công!");
        }
        return ResponseEntity.badRequest().body("Không được phép xóa ADMIN!");
    }

    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    @PutMapping("/{id}/password")
    public ResponseEntity<String> repassUser(@PathVariable Long id, @RequestBody User character) {
        String newPassword = character.getPassword();
    
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        
        if (!newPassword.matches(passwordPattern)) {
            return ResponseEntity.badRequest().body("Mật khẩu quá yếu. Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số.");
        }

        User updated = userService.repassUser(id, character);
        if (updated != null && (updated.getTrangthai().equals("Chưa kích hoạt"))) {
            return ResponseEntity.ok("Dổi mật khẩu thành công. Vui lòng đăng nhập lại!");
        }
        return ResponseEntity.notFound().build();
    }
}
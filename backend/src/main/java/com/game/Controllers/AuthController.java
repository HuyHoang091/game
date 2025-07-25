package com.game.Controllers;

import com.game.Config.JwtUtil;
import com.game.Model.User;
import com.game.Repository.ResetTokenRepository;
import com.game.Model.AuthResponse;
import com.game.Model.ResetToken;
import com.game.Service.AppCodeService;
import com.game.Service.UserService;
import com.game.Utils.IpUtils;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AppCodeService appCodeService;

    @Autowired
    private ResetTokenRepository resetTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.code}")
    private String APP_CODE;

    @Value("${app.part1}")
    private String PART1;

     @Value("${app.repass}")
    private String REPASS;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest, @RequestHeader("App-Code") String part1, @RequestHeader("Session-Hash") String SessionHash, HttpServletRequest request) {
        if (!part1.equals(PART1)) {
            appCodeService.Fail(loginRequest.getUsername());
            logger.warn("Cảnh báo người dùng [{}] đã sử dụng ứng dụng giả mạo! IP: [{}]", loginRequest.getUsername(), IpUtils.getClientIp(request));
            return ResponseEntity.status(408).body("Bạn đang dùng ứng dụng không hợp lệ nếu còn vi phạm chúng tôi sẽ khóa tài khoản này vĩnh viễn!");
        }
        if (!appCodeService.validateSessionCode(SessionHash)) {
            return ResponseEntity.status(419).body("Hết phiên đăng nhập!");
        }
        User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (user != null) {
            String sessionId = UUID.randomUUID().toString();
            user.setSessionId(sessionId);
            userService.updateUser(user.getId(), user);
            String token = jwtUtil.generateToken(user.getUsername(), sessionId);
            appCodeService.startAppCodeTimeout(loginRequest.getUsername());
            appCodeService.initAppCode(loginRequest.getUsername(), part1);
            appCodeService.invalidateSessionCode(SessionHash);
            return ResponseEntity.ok(new AuthResponse(user, token));
        }
        return ResponseEntity.badRequest().body("Tài khoản hoặc mật khẩu không đúng!");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty() || user.getEmail() == null || user.getEmail().isEmpty()) {
            return new ResponseEntity<>("Không được bỏ trống Username, Email.", HttpStatus.BAD_REQUEST);
        }

        User registeredUser = userService.registerUser(user);
        if (registeredUser != null) {
            return new ResponseEntity<>("Mật khẩu đã được gửi về email của bạn!!!(Lưu ý: hãy xác nhận tài khoản trong 24h tới!!!)", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Đăng ký không thành công. Tên người dùng hoặc email có thể đã tồn tại.", HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody User user) {
        boolean loggedOut = userService.logout(user.getUsername());
        if (loggedOut) {
            appCodeService.cancelTasksForUser(user.getUsername());
            return ResponseEntity.ok("Đăng xuất thành công!");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Đăng xuất không thành công! Người dùng không tồn tại hoặc đã đăng xuất trước đó.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> repass(@RequestBody User user, @RequestHeader("App-Code") String appCode) {
        if (!appCode.equals(REPASS)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid app code");
        }
        String token = jwtUtil.generateResetToken(user.getEmail());
        ResetToken resetToken = new ResetToken();
        resetToken.setEmail(user.getEmail());
        resetToken.setToken(token);
        resetToken.setUsed(false);
        resetTokenRepository.save(resetToken);

        boolean repass = userService.repass(user.getEmail(), token);
        if (repass) {
            return ResponseEntity.ok("Link đổi mật khẩu đã được gửi đến email của bạn.");
        }
        return ResponseEntity.badRequest().body("Không gửi được email! Hãy kiểm tra lại địa chỉ email.");
    }

    @GetMapping("/reset-password")
    public ResponseEntity<String> showResetForm(@RequestParam("token") String token) {
        ResetToken resetToken = resetTokenRepository.findByToken(token);
        if (resetToken == null || resetToken.isUsed()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Link đã hết hạn hoặc không hợp lệ");
        }
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Link đã hết hạn hoặc không hợp lệ");
        }

        String formHtml = "<html><head>"
                + "<style>"
                + "body {"
                + "  display: flex; justify-content: center; align-items: center; height: 100vh;"
                + "  background-color: #f4f4f4; font-family: Arial, sans-serif;"
                + "}"
                + "form {"
                + "  background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);"
                + "  display: flex; flex-direction: column; width: 300px;"
                + "}"
                + "input[type='password'] {"
                + "  margin-bottom: 15px; padding: 10px; border: 1px solid #ccc; border-radius: 5px;"
                + "}"
                + "button {"
                + "  padding: 10px; background-color: #4CAF50; color: white;"
                + "  border: none; border-radius: 5px; cursor: pointer;"
                + "}"
                + "button:hover { background-color: #45a049; }"
                + "</style>"
                + "</head><body>"
                + "<form action='/api/auth/reset-password' method='POST' onsubmit='return validateForm()'>"
                + "<input type='hidden' name='token' value='" + token + "'/>"
                + "<input type='password' id='newPassword' name='newPassword' placeholder='Mật khẩu mới' required/>"
                + "<input type='password' id='confirmPassword' placeholder='Nhập lại mật khẩu' required/>"
                + "<button type='submit'>Đặt lại mật khẩu</button>"
                + "</form>"
                + "<script>"
                + "function validateForm() {"
                + "  var newPass = document.getElementById('newPassword').value;"
                + "  var confirmPass = document.getElementById('confirmPassword').value;"
                + "  if (newPass !== confirmPass) {"
                + "    alert('Mật khẩu nhập lại không khớp.');"
                + "    return false;"
                + "  }"
                + "  return true;"
                + "}"
                + "</script>"
                + "</body></html>";

        return ResponseEntity.ok(formHtml);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
        ResetToken resetToken = resetTokenRepository.findByToken(token);
        if (resetToken == null || resetToken.isUsed()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Link đã hết hạn hoặc không hợp lệ");
        }
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("Token hết hạn");
        }

        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        
        if (!newPassword.matches(passwordPattern)) {
            return ResponseEntity.badRequest().body("Mật khẩu quá yếu. Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số.");
        }

        String email = jwtUtil.getUsernameFromToken(token);
        User user = userService.resetpass(email, newPassword);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng với email: " + email);
        }
        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);
        
        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }
}

package com.game.Service;

import com.game.Model.User;
import com.game.Repository.UserRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired // Đảm bảo dòng này tồn tại
    private PasswordEncoder passwordEncoder;

    @Autowired // <--- Đảm bảo dòng này tồn tại
    private EmailService emailService;

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null); // Trả về user nếu tìm thấy, nếu không trả về null
    }

    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    public User createUser(User character) {
        User username = userRepository.findByUsername(character.getUsername());
        User email = userRepository.findByEmail(character.getEmail());
        if (username != null || email != null) {
            return null;
        }
        return userRepository.save(character);
    }

    public User updateUser(Long id, User newChar) {
        return userRepository.save(newChar);
    }
    
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public User registerUser(User user) {
        // 1. Kiểm tra xem username hoặc email đã tồn tại chưa
        if (userRepository.findByUsername(user.getUsername()) != null) {
            System.err.println("Registration failed: Username '" + user.getUsername() + "' already exists.");
            return null; // Hoặc throw một ngoại lệ tùy chỉnh
        }
        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()) != null) {
             System.err.println("Registration failed: Email '" + user.getEmail() + "' already exists.");
             return null; // Hoặc throw một ngoại lệ tùy chỉnh
        }

        // 2. Tự động tạo mật khẩu ngẫu nhiên
        String generatedPassword = generateRandomPassword(10); // Tạo mật khẩu 10 ký tự
        System.out.println("Generated password for " + user.getUsername() + ": " + generatedPassword); // CHỈ ĐỂ DEBUG, KHÔNG IN RA LOG THẬT!

        // 3. Mã hóa mật khẩu trước khi lưu
        // String encodedPassword = passwordEncoder.encode(generatedPassword);
        user.setPassword(generatedPassword);

        // 4. Lưu người dùng vào cơ sở dữ liệu
        User savedUser = userRepository.save(user);

        // 5. Gửi mật khẩu đã tạo đến email của người dùng
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            String subject = "Mật khẩu đăng ký tài khoản Game của bạn";
            String body = "Chào mừng bạn đến với Game của chúng tôi, " + user.getUsername() + "!\n\n"
                        + "Mật khẩu tạm thời của bạn là: " + generatedPassword + "\n\n"
                        + "Chúng tôi khuyến nghị bạn thay đổi mật khẩu này sau khi đăng nhập lần đầu.\n\n"
                        + "Trân trọng,\n"
                        + "Đội ngũ phát triển Game.";
            try {
                emailService.sendEmail(user.getEmail(), subject, body);
            } catch (Exception e) {
                System.err.println("Failed to send registration email to " + user.getEmail() + ": " + e.getMessage());
                // Xử lý lỗi gửi email (ví dụ: log lại, rollback giao dịch nếu cần)
            }
        } else {
            System.err.println("User " + user.getUsername() + " does not have an email address to send password to.");
        }

        return savedUser;
    }

    /**
     * Generates a random alphanumeric password.
     * @param length The desired length of the password.
     * @return A randomly generated password.
     */
    private String generateRandomPassword(int length) {
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";

        String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER;
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(PASSWORD_CHARS.length());
            sb.append(PASSWORD_CHARS.charAt(randomIndex));
        }
        return sb.toString();
    }
}
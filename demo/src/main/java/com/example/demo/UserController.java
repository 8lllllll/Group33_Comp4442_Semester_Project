package com.example.demo;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final LoginAccessLogRepository loginAccessLogRepository;
    private final JwtUtil jwtUtil;

    public UserController(
            UserRepository userRepository,
            LoginAccessLogRepository loginAccessLogRepository,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.loginAccessLogRepository = loginAccessLogRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    //create a user at powershell terminal
    //Invoke-RestMethod -Method POST -Uri "http://localhost:9001/api/users" `
    //-ContentType "application/json" `
    //-Body '{"username":"test","password":"test123","role":"USER"}'
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }
    
    //Test Login
    //Invoke-RestMethod -Method POST -Uri "http://localhost:9001/api/login" `
    //-ContentType "application/json" `
    //-Body '{"username":"test","password":"test123"}'
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername());

        if (user == null) {
            saveLoginAccessLog(request.getUsername(), null, false, "User not found");
            return new LoginResponse("User not found", null);
        }

        if (!user.getPassword().equals(request.getPassword())) {
            saveLoginAccessLog(user.getUsername(), user.getRole(), false, "Invalid password");
            return new LoginResponse("Invalid password", null);
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        saveLoginAccessLog(user.getUsername(), user.getRole(), true, "Login successful");
        return new LoginResponse("Login successful", token);
    }

    private void saveLoginAccessLog(String username, String role, boolean success, String message) {
        LoginAccessLog log = new LoginAccessLog();
        log.setUsername(username == null ? "UNKNOWN" : username);
        log.setRole(role);
        log.setSuccess(success);
        log.setMessage(message);
        loginAccessLogRepository.save(log);
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginResponse {
        private String message;
        private String token;

        public LoginResponse(String message, String token) {
            this.message = message;
            this.token = token;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}

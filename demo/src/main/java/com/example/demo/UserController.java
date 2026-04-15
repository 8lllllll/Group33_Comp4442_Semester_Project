package com.example.demo;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/access")
    public List<LoginAccessLog> getLoginAccessLogs() {
        return loginAccessLogRepository.findAll(Sort.by(Sort.Direction.DESC, "loginTime"));
    }

    @GetMapping("/profile")
    public ProfileResponse getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return null;
        }

        List<LoginAccessLogItem> loginRecords = loginAccessLogRepository.findByUsernameOrderByLoginTimeDesc(username)
                .stream()
                .map(log -> new LoginAccessLogItem(
                        log.getLoginTime(),
                        log.isSuccess(),
                        log.getMessage()))
                .collect(Collectors.toList());

        return new ProfileResponse(user.getId(), user.getUsername(), user.getRole(), loginRecords);
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
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername());

        if (user == null) {
            saveLoginAccessLog(request.getUsername(), null, false, "User not found");
            return ResponseEntity.status(401).body(new LoginResponse("User not found", null));
        }

        if (!user.getPassword().equals(request.getPassword())) {
            saveLoginAccessLog(user.getUsername(), user.getRole(), false, "Invalid password");
            return ResponseEntity.status(401).body(new LoginResponse("Invalid password", null));
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        saveLoginAccessLog(user.getUsername(), user.getRole(), true, "Login successful");
        return ResponseEntity.ok(new LoginResponse("Login successful", token));
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

    public static class ProfileResponse {
        private Long id;
        private String name;
        private String role;
        private List<LoginAccessLogItem> loginRecords;

        public ProfileResponse(Long id, String name, String role, List<LoginAccessLogItem> loginRecords) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.loginRecords = loginRecords;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public List<LoginAccessLogItem> getLoginRecords() {
            return loginRecords;
        }

        public void setLoginRecords(List<LoginAccessLogItem> loginRecords) {
            this.loginRecords = loginRecords;
        }
    }

    public static class LoginAccessLogItem {
        private java.time.LocalDateTime loginTime;
        private boolean success;
        private String message;

        public LoginAccessLogItem(java.time.LocalDateTime loginTime, boolean success, String message) {
            this.loginTime = loginTime;
            this.success = success;
            this.message = message;
        }

        public java.time.LocalDateTime getLoginTime() {
            return loginTime;
        }

        public void setLoginTime(java.time.LocalDateTime loginTime) {
            this.loginTime = loginTime;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

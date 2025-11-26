package app.Controller;

import app.dto.LoginRequest;
import app.dto.LoginResponse;
import app.dto.LogoutRequest;
import app.dto.SessionValidationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        HttpStatus status = response.status != null
                ? response.status
                : (response.success ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(status).body(response);
    }
    @PostMapping("/validate")
    public ResponseEntity<LoginResponse> validate(@RequestBody SessionValidationRequest request) {
        LoginResponse response = authService.validateSession(request);
        HttpStatus status = response.status != null
                ? response.status
                : (response.success ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public String health() {
        return "Auth service is running";
    }
}
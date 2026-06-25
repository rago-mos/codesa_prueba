package codesa.com.co.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesa.com.co.dto.LoginRequest;
import codesa.com.co.dto.ProfileResponse;
import codesa.com.co.dto.RegisterRequest;
import codesa.com.co.dto.TokenResponse;
import codesa.com.co.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
		TokenResponse token = authService.login(request.username(), request.password());
		return ResponseEntity.ok(token);
	}

	@PostMapping("/register")
	public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
		authService.register(request.username(), request.password());
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("/profile")
	public ResponseEntity<ProfileResponse> profile(@RequestHeader("X-Auth-User") String username) {
		ProfileResponse profile = authService.getProfile(username);
		return ResponseEntity.ok(profile);
	}
}

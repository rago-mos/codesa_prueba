package codesa.com.co.service;

import codesa.com.co.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import codesa.com.co.domain.Role;
import codesa.com.co.domain.User;
import codesa.com.co.dto.ProfileResponse;
import codesa.com.co.dto.TokenResponse;
import codesa.com.co.repository.UserRepository;
import codesa.com.co.security.JwtService;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final UserMapper userMapper;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.userMapper = userMapper;
	}

	public TokenResponse login(String username, String password) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new IllegalArgumentException("Invalid credentials");
		}

		String token = jwtService.generateToken(username, user.getRole().name());
		return new TokenResponse(token, "Bearer", jwtService.getExpirationMs(), username, user.getRole().name());
	}

	public ProfileResponse getProfile(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		return new ProfileResponse(user.getId(), user.getUsername(), user.getRole().name());
	}

	public void register(String username, String password) {
		if (userRepository.existsByUsername(username)) {
			throw new IllegalArgumentException("Username already exists");
		}
		User user = userMapper.toEntity(username, password);
		userRepository.save(user);
	}
}

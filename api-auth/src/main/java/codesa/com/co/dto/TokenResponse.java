package codesa.com.co.dto;

public record TokenResponse(
		String token,
		String tokenType,
		long expiresInMs,
		String username,
		String role) {
}

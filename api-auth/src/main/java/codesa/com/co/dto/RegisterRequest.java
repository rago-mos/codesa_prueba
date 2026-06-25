package codesa.com.co.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank String username,
		@NotBlank @Size(min = 4) String password) {
}

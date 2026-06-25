package codesa.com.co.dto;

import jakarta.validation.constraints.NotBlank;

public record TareaRequest(
		@NotBlank String titulo,
		@NotBlank String descripcion) {
}
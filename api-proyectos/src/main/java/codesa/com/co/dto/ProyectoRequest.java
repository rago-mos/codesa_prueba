package codesa.com.co.dto;

import jakarta.validation.constraints.NotBlank;

public record ProyectoRequest(
		@NotBlank String nombre,
		@NotBlank String descripcion) {
}
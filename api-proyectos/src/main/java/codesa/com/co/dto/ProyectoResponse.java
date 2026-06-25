package codesa.com.co.dto;

public record ProyectoResponse(
		Long id,
		String nombre,
		String descripcion,
		String owner,
		String estado) {
}
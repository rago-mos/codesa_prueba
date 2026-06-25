package codesa.com.co.dto;

public record TareaResponse(
		Long id,
		String titulo,
		String descripcion,
		String estado,
		Long proyectoId) {
}
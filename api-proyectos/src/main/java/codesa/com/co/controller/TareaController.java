package codesa.com.co.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesa.com.co.domain.Tarea;
import codesa.com.co.dto.TareaRequest;
import codesa.com.co.dto.TareaResponse;
import codesa.com.co.service.TareaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{proyectoId}/tasks")
public class TareaController {

	private final TareaService tareaService;

	public TareaController(TareaService tareaService) {
		this.tareaService = tareaService;
	}

	@PostMapping
	public ResponseEntity<TareaResponse> crear(@PathVariable Long proyectoId, @Valid @RequestBody TareaRequest request) {
		Tarea tarea = tareaService.crear(proyectoId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(tarea));
	}

	@GetMapping
	public ResponseEntity<List<TareaResponse>> listar(@PathVariable Long proyectoId) {
		List<Tarea> tareas = tareaService.listarPorProyecto(proyectoId);
		return ResponseEntity.ok(tareas.stream().map(this::toResponse).toList());
	}

	@GetMapping("/{id}")
	public ResponseEntity<TareaResponse> obtener(@PathVariable Long proyectoId, @PathVariable Long id) {
		Tarea tarea = tareaService.obtener(id);
		return ResponseEntity.ok(toResponse(tarea));
	}

	@PutMapping("/{id}")
	public ResponseEntity<TareaResponse> actualizar(@PathVariable Long proyectoId, @PathVariable Long id,
			@Valid @RequestBody TareaRequest request) {
		Tarea tarea = tareaService.actualizar(id, request);
		return ResponseEntity.ok(toResponse(tarea));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long proyectoId, @PathVariable Long id) {
		tareaService.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	private TareaResponse toResponse(Tarea tarea) {
		return new TareaResponse(tarea.getId(), tarea.getTitulo(), tarea.getDescripcion(), tarea.getEstado(),
				tarea.getProyecto().getId());
	}
}
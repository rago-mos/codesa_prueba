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

import codesa.com.co.domain.Proyecto;
import codesa.com.co.dto.ProyectoRequest;
import codesa.com.co.dto.ProyectoResponse;
import codesa.com.co.service.ProyectoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class ProyectoController {

	private final ProyectoService proyectoService;

	public ProyectoController(ProyectoService proyectoService) {
		this.proyectoService = proyectoService;
	}

	@PostMapping
	public ResponseEntity<ProyectoResponse> crear(@Valid @RequestBody ProyectoRequest request) {
		Proyecto proyecto = proyectoService.crear(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(proyecto));
	}

	@GetMapping
	public ResponseEntity<List<ProyectoResponse>> listar() {
		List<Proyecto> proyectos = proyectoService.listar();
		return ResponseEntity.ok(proyectos.stream().map(this::toResponse).toList());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProyectoResponse> obtener(@PathVariable Long id) {
		Proyecto proyecto = proyectoService.obtener(id);
		return ResponseEntity.ok(toResponse(proyecto));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProyectoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ProyectoRequest request) {
		Proyecto proyecto = proyectoService.actualizar(id, request);
		return ResponseEntity.ok(toResponse(proyecto));
	}

	@PutMapping("/{id}/archive")
	public ResponseEntity<Void> archivar(@PathVariable Long id) {
		proyectoService.archivar(id);
		return ResponseEntity.noContent().build();
	}

	private ProyectoResponse toResponse(Proyecto proyecto) {
		return new ProyectoResponse(proyecto.getId(), proyecto.getNombre(), proyecto.getDescripcion(),
				proyecto.getOwner(), proyecto.getEstado().name());
	}
}
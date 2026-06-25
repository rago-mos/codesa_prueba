package codesa.com.co.service;

import java.util.List;

import codesa.com.co.mapper.TareaMapper;
import org.springframework.stereotype.Service;

import codesa.com.co.domain.Proyecto;
import codesa.com.co.domain.Tarea;
import codesa.com.co.dto.TareaRequest;
import codesa.com.co.repository.TareaRepository;

@Service
public class TareaService {

	private final TareaRepository tareaRepository;
	private final ProyectoService proyectoService;
	private final TareaMapper tareaMapper;

	public TareaService(TareaRepository tareaRepository, ProyectoService proyectoService, TareaMapper tareaMapper) {
		this.tareaRepository = tareaRepository;
		this.proyectoService = proyectoService;
		this.tareaMapper = tareaMapper;
	}

	public Tarea crear(Long proyectoId, TareaRequest request) {
		Proyecto proyecto = proyectoService.obtener(proyectoId);

		if (proyecto.getEstado().toString().equals("ARCHIVED")) {
			throw new IllegalArgumentException("No se pueden crear tareas en proyectos archivados");
		}

		Tarea tarea = tareaMapper.toEntity(request, proyecto);
		return tareaRepository.save(tarea);
	}

	public List<Tarea> listarPorProyecto(Long proyectoId) {
		proyectoService.obtener(proyectoId);
		return tareaRepository.findByProyectoId(proyectoId);
	}

	public Tarea obtener(Long id) {
		return tareaRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada"));
	}

	public Tarea actualizar(Long id, TareaRequest request) {
		Tarea tarea = obtener(id);
		tarea.setTitulo(request.titulo());
		tarea.setDescripcion(request.descripcion());
		return tareaRepository.save(tarea);
	}

	public void eliminar(Long id) {
		tareaRepository.deleteById(id);
	}
}
package codesa.com.co.service;

import java.util.List;

import codesa.com.co.mapper.ProyectoMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import codesa.com.co.domain.Estado;
import codesa.com.co.domain.Proyecto;
import codesa.com.co.dto.ProyectoRequest;
import codesa.com.co.repository.ProyectoRepository;

@Service
public class ProyectoService {

	private final ProyectoRepository proyectoRepository;
	private final ProyectoMapper proyectoMapper;

	public ProyectoService(ProyectoRepository proyectoRepository, ProyectoMapper proyectoMapper) {
		this.proyectoRepository = proyectoRepository;
		this.proyectoMapper = proyectoMapper;
	}

	public Proyecto crear(ProyectoRequest request) {
		String owner = getUsuario();
		Proyecto proyecto = proyectoMapper.toEntity(request, owner);
		return proyectoRepository.save(proyecto);
	}

	public List<Proyecto> listar() {
		String usuario = getUsuario();

		if (esAdmin()) {
			return proyectoRepository.findAll();
		}
		return proyectoRepository.findByOwner(usuario);
	}

	public Proyecto obtener(Long id) {
		String usuario = getUsuario();
		Proyecto proyecto = proyectoRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));


		boolean esOwner = proyecto.getOwner().equals(usuario);
		boolean tieneAcceso = esAdmin() || esOwner;

		if (!tieneAcceso) {
			throw new AccessDeniedException("Proyecto " + id + " pertenece a otro usuario. Solo el dueño o un ADMIN pueden acceder.");
		}
		return proyecto;
	}

	public Proyecto actualizar(Long id, ProyectoRequest request) {
		Proyecto proyecto = obtener(id);
		proyecto.setNombre(request.nombre());
		proyecto.setDescripcion(request.descripcion());
		return proyectoRepository.save(proyecto);
	}

	public void archivar(Long id) {
		Proyecto proyecto = obtener(id);
		proyecto.setEstado(Estado.ARCHIVED);
		proyectoRepository.save(proyecto);
	}

	private String getUsuario() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null ? auth.getName() : null;
	}

	private boolean esAdmin() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null && auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	}
}
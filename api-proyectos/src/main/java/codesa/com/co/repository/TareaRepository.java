package codesa.com.co.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import codesa.com.co.domain.Tarea;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

	List<Tarea> findByProyectoId(Long proyectoId);
}
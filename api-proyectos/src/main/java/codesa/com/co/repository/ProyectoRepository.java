package codesa.com.co.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import codesa.com.co.domain.Proyecto;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

	List<Proyecto> findByOwner(String owner);

	Optional<Proyecto> findByIdAndOwner(Long id, String owner);
}
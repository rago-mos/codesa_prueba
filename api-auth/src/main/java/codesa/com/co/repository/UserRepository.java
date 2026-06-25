package codesa.com.co.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import codesa.com.co.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);
}

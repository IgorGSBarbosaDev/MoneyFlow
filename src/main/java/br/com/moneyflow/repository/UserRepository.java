package br.com.moneyflow.repository;

import br.com.moneyflow.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByEmailAndActive(String email, boolean active);
    List<User> findAllByActive(boolean active);
    long countByActiveTrue();
    Optional<User> findByEmailIgnoreCase(String email);
    List<User> findAllByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}

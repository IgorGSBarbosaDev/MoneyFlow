package br.com.moneyflow.repository;

import br.com.moneyflow.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<User> findByEmailAndActive(String email, boolean active);
    List<User> findAllByActive(boolean active);
    long countByActiveTrue();
    Optional<User> findByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User>findAllUsersCreatedBetweenDates(LocalDateTime starDate, LocalDateTime endDate);

}

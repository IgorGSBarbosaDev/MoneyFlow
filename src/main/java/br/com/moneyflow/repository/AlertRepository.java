package br.com.moneyflow.repository;

import br.com.moneyflow.model.entity.Alert;
import br.com.moneyflow.model.entity.AlertLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {


    List<Alert> findByUserId(Long userId);
    List<Alert> findByUserIdAndReadFalse(Long userId);
    List<Alert> findByUserIdAndLevel(Long userId, AlertLevel level);
    Long countByUserIdAndReadFalse(Long userId);
    boolean existsByUserIdAndBudgetIdAndLevel(Long userId, Long budgetId, AlertLevel level);

    @Query("""
            SELECT a FROM Alert a
            WHERE a.user.id = :userId
            AND (:read IS NULL OR a.read = :read)
            ORDER BY
                CASE a.level
                    WHEN br.com.moneyflow.model.entity.AlertLevel.CRITICAL THEN 1
                    WHEN br.com.moneyflow.model.entity.AlertLevel.WARNING THEN 2
                    WHEN br.com.moneyflow.model.entity.AlertLevel.INFO THEN 3
                END,
                a.createdAt DESC
            """)
    List<Alert> findByUserIdOrderedByPriorityAndDate(
            @Param("userId") Long userId,
            @Param("read") Boolean read
    );

    @Query("""
            SELECT a FROM Alert a
            WHERE a.user.id = :userId
            AND a.createdAt BETWEEN :startDate AND :endDate
            ORDER BY a.createdAt DESC
            """)
    List<Alert> findByUserIdAndPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Modifying
    @Query("""
            DELETE FROM Alert a
            WHERE a.user.id = :userId
            AND a.read = true
            AND a.createdAt < :cutoffDate
            """)
    int deleteOldReadAlerts(
            @Param("userId") Long userId,
            @Param("cutoffDate") LocalDateTime cutoffDate
    );

    @Modifying
    @Query("""
            UPDATE Alert a
            SET a.read = true
            WHERE a.user.id = :userId
            AND a.id IN :alertIds
            """)
    int markAlertsAsRead(
            @Param("userId") Long userId,
            @Param("alertIds") List<Long> alertIds
    );
}

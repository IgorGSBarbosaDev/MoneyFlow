package br.com.moneyflow.repository;

import br.com.moneyflow.model.dto.projection.BudgetStatusProjection;
import br.com.moneyflow.model.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.category.id = :categoryId")
    List<Budget> findByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    boolean existsByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);

    @Query("SELECT b FROM Budget b " +
            "WHERE b.user.id = :userId " +
            "AND b.category.id = :categoryId " +
            "AND b.month = :month " +
            "AND b.year = :year")
    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("month") Integer month,
            @Param("year") Integer year);

    @Query("SELECT b FROM Budget b WHERE b.id = :budgetId AND b.user.id = :userId")
    Optional<Budget> findByIdAndUserId(@Param("budgetId") Long budgetId, @Param("userId") Long userId);

    @Query("SELECT " +
            "b.id as id, " +
            "b.amount as budgetAmount, " +
            "c.id as categoryId, " +
            "c.name as categoryName, " +
            "COALESCE(SUM(t.amount), 0) as spentAmount " +
            "FROM Budget b " +
            "JOIN b.category c " +
            "LEFT JOIN Transaction t ON t.category.id = c.id " +
            "  AND t.type = 'EXPENSE' " +
            "  AND t.deleted = false " +
            "  AND YEAR(t.date) = b.year " +
            "  AND MONTH(t.date) = b.month " +
            "  AND t.user.id = b.user.id " +
            "WHERE b.user.id = :userId " +
            "AND b.month = :month " +
            "AND b.year = :year " +
            "GROUP BY b.id, b.amount, c.id, c.name")
    List<BudgetStatusProjection> findBudgetStatusByUserAndPeriod(
            @Param("userId") Long userId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    @Query("SELECT " +
            "b.id as id, " +
            "b.amount as budgetAmount, " +
            "c.id as categoryId, " +
            "c.name as categoryName, " +
            "COALESCE(SUM(t.amount), 0) as spentAmount " +
            "FROM Budget b " +
            "JOIN b.category c " +
            "LEFT JOIN Transaction t ON t.category.id = c.id " +
            "  AND t.type = 'EXPENSE' " +
            "  AND t.deleted = false " +
            "  AND YEAR(t.date) = b.year " +
            "  AND MONTH(t.date) = b.month " +
            "  AND t.user.id = b.user.id " +
            "WHERE b.id = :budgetId " +
            "AND b.user.id = :userId " +
            "GROUP BY b.id, b.amount, c.id, c.name")
    Optional<BudgetStatusProjection> findBudgetStatusByIdAndUserId(
            @Param("budgetId") Long budgetId,
            @Param("userId") Long userId
    );
}

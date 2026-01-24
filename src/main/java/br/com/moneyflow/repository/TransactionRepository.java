package br.com.moneyflow.repository;

import br.com.moneyflow.model.entity.Transaction;
import br.com.moneyflow.model.entity.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Query Methods (Derivados)
    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    List<Transaction> findByUserIdAndType(Long userId, TransactionType type);

    List<Transaction> findByUserIdAndCategoryId(Long userId, Long categoryId);

    Boolean existsByCategoryIdAndDeletedFalse(Long categoryId);

    // Queries Customizadas (@Query)

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.deleted = false " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "ORDER BY t.date DESC")
    List<Transaction> findByUserIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.deleted = false " +
            "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
            "AND (:type IS NULL OR t.type = :type) " +
            "AND (:startDate IS NULL OR t.date >= :startDate) " +
            "AND (:endDate IS NULL OR t.date <= :endDate) " +
            "ORDER BY t.date DESC")
    List<Transaction> findByFilters(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.type = 'INCOME' " +
            "AND t.deleted = false " +
            "AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumIncomeByPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.type = 'EXPENSE' " +
            "AND t.deleted = false " +
            "AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumExpensesByPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.category.id = :categoryId " +
            "AND t.type = 'EXPENSE' " +
            "AND t.deleted = false " +
            "AND YEAR(t.date) = :year " +
            "AND MONTH(t.date) = :month")
    BigDecimal sumExpensesByCategoryAndMonth(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("year") int year,
            @Param("month") int month
    );

    @Query("SELECT c.id as categoryId, c.name as categoryName, SUM(t.amount) as totalAmount, COUNT(t.id) as transactionCount " +
            "FROM Transaction t " +
            "JOIN t.category c " +
            "WHERE t.user.id = :userId " +
            "AND t.deleted = false " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "GROUP BY c.id, c.name " +
            "ORDER BY totalAmount DESC")
    List<CategoryExpenseProjection> findExpensesByCategory(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT YEAR(t.date) as year, MONTH(t.date) as month, t.type as type, SUM(t.amount) as total " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.deleted = false " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(t.date), MONTH(t.date), t.type " +
            "ORDER BY year DESC, month DESC")
    List<MonthlyComparisonProjection> findMonthlyComparison(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.deleted = false " +
            "ORDER BY t.date DESC, t.createdAt DESC")
    List<Transaction> findRecentTransactions(
            @Param("userId") Long userId,
            Pageable pageable
    );

    // Interfaces de Projeção
    interface CategoryExpenseProjection {
        Long getCategoryId();
        String getCategoryName();
        BigDecimal getTotalAmount();
        Integer getTransactionCount();
    }

    interface MonthlyComparisonProjection {
        Integer getYear();
        Integer getMonth();
        TransactionType getType();
        BigDecimal getTotal();
    }
}

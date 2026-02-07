package br.com.moneyflow.repository;

import br.com.moneyflow.model.entity.Category;
import br.com.moneyflow.model.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdAndType(Long userId, CategoryType type);

    Boolean existsByUserIdAndName(Long userId, String name);

    Optional<Category> findByUserIdAndId(Long userId, Long id);

    Long countByUserId(Long userId);

    @Query("SELECT c, COUNT(t.id) as transactionCount " +
            "FROM Category c " +
            "LEFT JOIN Transaction t ON t.category.id = c.id " +
            "WHERE c.user.id = :userId " +
            "GROUP BY c.id")
    List<Object[]> findAllWithTransactionCountByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Category c " +
            "WHERE c.user.id = :userId " +
            "AND c.id NOT IN (" +
            "    SELECT DISTINCT t.category.id FROM Transaction t WHERE t.deleted = false" +
            ")")
    List<Category> findCategoriesWithoutTransactions(@Param("userId") Long userId);

    @Query("SELECT c.id, c.name, COUNT(t.id) as usageCount " +
            "FROM Category c " +
            "JOIN Transaction t ON t.category.id = c.id " +
            "WHERE c.user.id = :userId AND t.deleted = false " +
            "GROUP BY c.id, c.name " +
            "ORDER BY usageCount DESC " +
            "LIMIT 5")
    List<Object[]> findTopCategoriesByUsage(@Param("userId") Long userId);
}

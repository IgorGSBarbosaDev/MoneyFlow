package br.com.moneyflow.service;

import br.com.moneyflow.exception.*;
import br.com.moneyflow.model.dto.category.CategoryRequestDTO;
import br.com.moneyflow.model.dto.category.CategoryResponseDTO;
import br.com.moneyflow.model.dto.category.CategoryWithCountDTO;
import br.com.moneyflow.model.entity.Category;
import br.com.moneyflow.model.entity.CategoryType;
import br.com.moneyflow.model.entity.User;
import br.com.moneyflow.repository.BudgetRepository;
import br.com.moneyflow.repository.CategoryRepository;
import br.com.moneyflow.repository.TransactionRepository;
import br.com.moneyflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    @Transactional
    public CategoryResponseDTO createCategory(Long userId, CategoryRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (categoryRepository.existsByUserIdAndName(userId, dto.name())) {
            throw new DuplicateCategoryException("Category with name '" + dto.name() + "' already exists for this user");
        }
        if (dto.type() == null) {
            throw new ValidationException("Category type cannot be null");
        }

        validateTextFormat(dto.name(), dto.color());

        Category category = new Category();
        category.setUser(user);
        category.setName(dto.name());
        category.setDescription(dto.description());
        category.setType(dto.type());
        category.setColor(dto.color());
        category.setIcon(dto.icon());

        Category savedCategory = categoryRepository.save(category);

        return toDTO(savedCategory);
    }

    public List<CategoryResponseDTO> getCategoriesByUser(Long userId, CategoryType type) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        List<Category> categories;
        if (type == null) {
            categories = categoryRepository.findByUserId(userId);
        } else {
            categories = categoryRepository.findByUserIdAndType(userId, type);
        }

        return categories.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CategoryResponseDTO getCategoryById(Long userId, Long categoryId) {
        Category category = findCategoryOrThrow(categoryId);

        if (!category.getUser().getId().equals(userId)) {
            throw new ValidationException("Unauthorized access: Category does not belong to this user");
        }
        return toDTO(category);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long userId, Long categoryId, CategoryRequestDTO dto) {
        Category category = findCategoryOrThrow(categoryId);

        if (!category.getUser().getId().equals(userId)) {
            throw new ValidationException("Unauthorized access: Category does not belong to this user");
        }

        if (!category.getName().equals(dto.name())) {
            if (categoryRepository.existsByUserIdAndName(userId, dto.name())) {
                throw new DuplicateCategoryException("Category with name '" + dto.name() + "' already exists for this user");
            }
        }

        if (category.getType() != dto.type()) {
            boolean hasTransactions = transactionRepository.existsByCategoryId(categoryId);
            if (hasTransactions) {
                throw new CategoryTypeChangeNotAllowedException(
                        "Cannot change category type because it has associated transactions. " +
                        "Please remove or reassign transactions first.");
            }
        }

        validateTextFormat(dto.name(), dto.color());

        category.setName(dto.name());
        category.setDescription(dto.description());
        category.setType(dto.type());
        category.setColor(dto.color());
        category.setIcon(dto.icon());

        Category updatedCategory = categoryRepository.save(category);

        return toDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = findCategoryOrThrow(categoryId);

        if (!category.getUser().getId().equals(userId)) {
            throw new ValidationException("Unauthorized access: Category does not belong to this user");
        }

        boolean hasTransactions = transactionRepository.existsByCategoryId(categoryId);
        if (hasTransactions) {
            throw new CategoryHasTransactionsException(
                    "Cannot delete category because it has associated transactions. " +
                    "Please reassign or delete the transactions first.");
        }

        List<?> budgets = budgetRepository.findByuserAndCategoryId(userId, categoryId);
        if (!budgets.isEmpty()) {
            throw new ValidationException(
                    "Cannot delete category because it has " + budgets.size() + " budget(s) associated. " +
                    "Please delete the budgets first.");
        }

        categoryRepository.delete(category);
    }

    public List<CategoryWithCountDTO> getCategoriesWithTransactionCount(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        List<Object[]> results = categoryRepository.findAllWithTransactionCountByUserId(userId);

        return results.stream()
                .map(result -> {
                    Category category = (Category) result[0];
                    Long transactionCount = ((Number) result[1]).longValue();

                    return new CategoryWithCountDTO(
                            category.getId(),
                            category.getName(),
                            category.getDescription(),
                            category.getType().toString(),
                            category.getColor(),
                            category.getIcon(),
                            category.getCreatedAt(),
                            category.getUpdatedAt(),
                            transactionCount
                    );
                })
                .collect(Collectors.toList());
    }

    private Category findCategoryOrThrow(Long categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));
    }

    private void validateTextFormat(String name, String color) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Category name cannot be null or blank");
        }
        if (name.length() < 2 || name.length() > 30) {
            throw new ValidationException("Category name must be between 2 and 30 characters");
        }
        if (color != null && !color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
            throw new ValidationException("Invalid color format. Must be hexadecimal (e.g., #FF5733 or #F57)");
        }
    }

    private CategoryResponseDTO toDTO(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getType(),
                category.getColor(),
                category.getIcon(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}

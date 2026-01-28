package br.com.moneyflow.service;

import br.com.moneyflow.exception.DuplicateCategoryException;
import br.com.moneyflow.exception.UserNotFoundException;
import br.com.moneyflow.exception.ValidationException;
import br.com.moneyflow.model.dto.category.CategoryRequestDTO;
import br.com.moneyflow.model.dto.category.CategoryResponseDTO;
import br.com.moneyflow.model.entity.Category;
import br.com.moneyflow.model.entity.CategoryType;
import br.com.moneyflow.model.entity.User;
import br.com.moneyflow.repository.CategoryRepository;
import br.com.moneyflow.repository.TransactionRepository;
import br.com.moneyflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public CategoryResponseDTO createCategory(Long userId, CategoryRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        doubleCheckCategoryName(userId, dto.name());
        validateType(dto.type());
        validateTextFormat(dto.name(), dto.color());

        Category category = new Category();
        category.setUser(user);
        category.setName(dto.name());
        category.setDescription(dto.description());
        category.setType(dto.type());
        category.setColor(dto.color());
        category.setIcon(dto.icon());

        categoryRepository.save(category);

        return toDTO(category);
    }

    private void doubleCheckCategoryName(Long userId, String name){
        if (categoryRepository.existsByUserIdAndName(userId, name)){
            throw new DuplicateCategoryException("Category already exists: " + name);
        }
    }
    private void validateType(CategoryType categoryType){
        if (categoryType == null){
            throw new ValidationException("Category type cannot be null");
        }
    }
    private void validateTextFormat(String name, String color){
        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("Category name cannot be null or blank");
        }
        if (color != null && !color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")){
            throw new IllegalArgumentException("Invalid color format: " + color);
        }

    }
    private CategoryResponseDTO toDTO(Category category){
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

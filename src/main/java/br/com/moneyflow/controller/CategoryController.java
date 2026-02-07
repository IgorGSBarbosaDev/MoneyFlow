package br.com.moneyflow.controller;

import br.com.moneyflow.config.security.CurrentUser;
import br.com.moneyflow.model.dto.category.CategoryRequestDTO;
import br.com.moneyflow.model.dto.category.CategoryResponseDTO;
import br.com.moneyflow.model.dto.category.CategoryWithCountDTO;
import br.com.moneyflow.model.entity.CategoryType;
import br.com.moneyflow.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Gerenciamento de categorias")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar nova categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Categoria com este nome já existe")
    })
    public ResponseEntity<CategoryResponseDTO> create(
            @CurrentUser Long userId,
            @Valid @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO response = categoryService.createCategory(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar categorias do usuário")
    @ApiResponse(responseCode = "200", description = "Lista de categorias")
    public ResponseEntity<List<CategoryResponseDTO>> list(
            @CurrentUser Long userId,
            @RequestParam(required = false) @Parameter(description = "Filtrar por tipo") CategoryType type) {
        List<CategoryResponseDTO> categories = categoryService.getCategoriesByUser(userId, type);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "403", description = "Acesso não autorizado")
    })
    public ResponseEntity<CategoryResponseDTO> getById(
            @CurrentUser Long userId,
            @PathVariable @Parameter(description = "ID da categoria") Long id) {
        CategoryResponseDTO response = categoryService.getCategoryById(userId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "409", description = "Nome já existe")
    })
    public ResponseEntity<CategoryResponseDTO> update(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO response = categoryService.updateCategory(userId, id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria excluída"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "409", description = "Categoria possui transações")
    })
    public ResponseEntity<Void> delete(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        categoryService.deleteCategory(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/with-count")
    @Operation(summary = "Listar categorias com contagem de transações")
    public ResponseEntity<List<CategoryWithCountDTO>> listWithCount(
            @CurrentUser Long userId) {
        List<CategoryWithCountDTO> categories = categoryService.getCategoriesWithTransactionCount(userId);
        return ResponseEntity.ok(categories);
    }
}

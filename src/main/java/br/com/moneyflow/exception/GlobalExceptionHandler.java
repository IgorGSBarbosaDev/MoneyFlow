package br.com.moneyflow.exception;

import br.com.moneyflow.exception.authorization.InvalidCredentialsException;
import br.com.moneyflow.exception.authorization.InvalidTokenException;
import br.com.moneyflow.exception.authorization.UnauthorizedAcessException;
import br.com.moneyflow.exception.base.BaseException;
import br.com.moneyflow.exception.business.*;
import br.com.moneyflow.exception.resource.*;
import br.com.moneyflow.model.dto.error.ErrorResponseDTO;
import br.com.moneyflow.model.dto.error.FieldErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({
        UserNotFoundException.class,
        CategoryNotFoundException.class,
        TransactionNotFoundException.class,
        BudgetNotFoundException.class,
        AlertNotFoundException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleNotFound(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        log.warn("Validation failed: {}", ex.getMessage());

        List<FieldErrorDTO> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDTO(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Dados de entrada inválidos")
                .errors(fieldErrors)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({
        ValidationException.class,
        InvalidAmountException.class,
        InvalidDateException.class,
        InvalidDateRangeException.class,
        InvalidMonthException.class,
        InvalidYearException.class,
        InvalidCategoryTypeException.class,
        InvalidTransactionTypeException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleBusinessLogic(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        log.warn("Business logic error: {}", ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({
        InvalidCredentialsException.class,
        InvalidTokenException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleUnauthorized(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler({
        UnauthorizedAcessException.class,
        InactiveUserException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleForbidden(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        log.warn("Forbidden access: {}", ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler({
        DuplicateEmailException.class,
        DuplicateCategoryException.class,
        BudgetAlreadyExistsException.class,
        CategoryTypeChangeNotAllowedException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleConflict(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        log.warn("Conflict: {}", ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponseDTO> handleBaseException(
            BaseException ex,
            HttpServletRequest request
    ) {
        log.warn("Base exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getHttpStatus().value())
                .error(ex.getHttpStatus().getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(ex.getHttpStatus()).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneral(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error: ", ex);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Erro inesperado no servidor")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}

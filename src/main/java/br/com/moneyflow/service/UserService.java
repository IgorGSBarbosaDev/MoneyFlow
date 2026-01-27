package br.com.moneyflow.service;

import br.com.moneyflow.exception.EmailAlreadyExistsException;
import br.com.moneyflow.exception.InvalidPasswordException;
import br.com.moneyflow.exception.UserNotFoundException;
import br.com.moneyflow.model.dto.user.ChangePasswordDTO;
import br.com.moneyflow.model.dto.user.UserRegistrationDTO;
import br.com.moneyflow.model.dto.user.UserResponseDTO;
import br.com.moneyflow.model.dto.user.UserUpdateDTO;
import br.com.moneyflow.model.entity.User;
import br.com.moneyflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO registerUser(UserRegistrationDTO dto) {
        validateEmailUniqueness(dto.email());

        validatePasswordStrength(dto.password());

        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));

        User savedUser = userRepository.save(user);

        return mapToResponseDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return mapToResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(Long userId, UserUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (dto.name() != null && !dto.name().isBlank()) {
            user.setName(dto.name());
        }

        if (dto.email() != null && !dto.email().isBlank()) {
            if (!dto.email().equals(user.getEmail())) {
                validateEmailUniqueness(dto.email());
                user.setEmail(dto.email());
            }
        }

        User updatedUser = userRepository.save(user);

        return mapToResponseDTO(updatedUser);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Senha atual incorreta");
        }
        if (!dto.newPassword().equals(dto.confirmPassword())) {
            throw new InvalidPasswordException("Nova senha e confirmação não coincidem");
        }

        validatePasswordStrength(dto.newPassword());

        if (passwordEncoder.matches(dto.newPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Nova senha deve ser diferente da senha atual");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.desactivate();

        userRepository.save(user);
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email já cadastrado: " + email);
        }
    }

    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new InvalidPasswordException("Senha deve ter no mínimo 8 caracteres");
        }

        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            throw new InvalidPasswordException(
                "Senha deve conter pelo menos uma letra e um número"
            );
        }
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getActive(),
            user.getCreatedAt()
        );
    }
}
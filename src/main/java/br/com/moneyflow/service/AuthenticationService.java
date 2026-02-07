package br.com.moneyflow.service;

import br.com.moneyflow.exception.authorization.InvalidCredentialsException;
import br.com.moneyflow.exception.business.InactiveUserException;
import br.com.moneyflow.model.dto.auth.AuthResponseDTO;
import br.com.moneyflow.model.dto.auth.LoginRequestDTO;
import br.com.moneyflow.model.dto.user.UserResponseDTO;
import br.com.moneyflow.model.entity.User;
import br.com.moneyflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public AuthResponseDTO authenticate(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!user.getActive()) {
            throw new InactiveUserException("Usuário inativo. Entre em contato com o suporte.");
        }

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getId());
        Long expiresIn = jwtService.getExpirationInSeconds();

        UserResponseDTO userResponse = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getActive(),
                user.getCreatedAt()
        );

        return new AuthResponseDTO(token, expiresIn, userResponse);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO refreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Usuário não encontrado"));

        if (!user.getActive()) {
            throw new InactiveUserException("Usuário inativo. Entre em contato com o suporte.");
        }

        String token = jwtService.generateToken(user.getId());
        Long expiresIn = jwtService.getExpirationInSeconds();

        UserResponseDTO userResponse = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getActive(),
                user.getCreatedAt()
        );

        return new AuthResponseDTO(token, expiresIn, userResponse);
    }
}

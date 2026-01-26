package br.com.moneyflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.moneyflow.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

private registerUser(UserRegistrationDTO dto){
    if (userRepository.existsByEmail(dto.getEmail()) != null) {
        throw new EmailAlreadyExistsException("Email already exists");
    }
    return null;
}










}

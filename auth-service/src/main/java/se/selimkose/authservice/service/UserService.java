package se.selimkose.authservice.service;

import org.springframework.stereotype.Service;
import se.selimkose.authservice.model.User;
import se.selimkose.authservice.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        // Logic to find user by email
        return userRepository.findByEmail(email);
    }
}

package se.selimkose.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.selimkose.authservice.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email); // Method to find user by email

}

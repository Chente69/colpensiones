package co.mycorp.security.spring_boot_security_jwt.respository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import co.mycorp.security.spring_boot_security_jwt.models.User;

public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}

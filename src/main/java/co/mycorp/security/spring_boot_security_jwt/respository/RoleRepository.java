package co.mycorp.security.spring_boot_security_jwt.respository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import co.mycorp.security.spring_boot_security_jwt.models.Role;
import co.mycorp.security.spring_boot_security_jwt.models.ERole;
public interface RoleRepository extends JpaRepository<Role, Long>{
    Optional<Role> findByName(ERole name);
}

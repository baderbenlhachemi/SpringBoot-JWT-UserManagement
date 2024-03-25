package ma.cirestechnologies.demo.repository;

import ma.cirestechnologies.demo.models.ERole;
import ma.cirestechnologies.demo.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}

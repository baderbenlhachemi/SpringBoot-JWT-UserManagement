package com.cirestechnologies.demo.repository;

import com.cirestechnologies.demo.model.ERole;
import com.cirestechnologies.demo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
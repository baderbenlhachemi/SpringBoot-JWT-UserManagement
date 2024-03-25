package com.cirestechnologies.demo.service;

import com.github.javafaker.Faker;
import com.cirestechnologies.demo.model.ERole;
import com.cirestechnologies.demo.model.Role;
import com.cirestechnologies.demo.model.User;
import com.cirestechnologies.demo.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;

@Service
public class FakeDataService {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    public User generateFakeUser() {

        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_ADMIN)));
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER)));

        Faker faker = new Faker();
        User user = new User();
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setBirthDate(faker.date().birthday());
        user.setCity(faker.address().city());
        user.setCountry(faker.address().country());
        user.setAvatar(faker.internet().avatar());
        user.setCompany(faker.company().name());
        user.setJobPosition(faker.company().profession());
        user.setMobile(faker.phoneNumber().cellPhone());
        user.setUsername(faker.name().username());
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(encoder.encode("password"));

        ERole randomRole = faker.bool().bool() ? ERole.ROLE_ADMIN : ERole.ROLE_USER;
        user.setRoles(new HashSet<>(Collections.singletonList(randomRole == ERole.ROLE_ADMIN ? adminRole : userRole)));

        return user;
    }

}
package com.cirestechnologies.demo.controller;

import com.cirestechnologies.demo.model.ERole;
import com.cirestechnologies.demo.model.Role;
import com.cirestechnologies.demo.model.User;
import com.cirestechnologies.demo.payload.request.LoginRequest;
import com.cirestechnologies.demo.payload.response.JwtResponse;
import com.cirestechnologies.demo.repository.RoleRepository;
import com.cirestechnologies.demo.repository.UserRepository;
import com.cirestechnologies.demo.security.jwt.JwtUtils;
import com.cirestechnologies.demo.security.services.UserDetailsImpl;
import com.cirestechnologies.demo.service.FakeDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private FakeDataService fakeDataService;

    @GetMapping("/users/generate/{count}")
    public ResponseEntity<FileSystemResource> generateUsers(@PathVariable int count, HttpServletResponse response) {
        // Generate user data
        List<User> users = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            users.add(fakeDataService.generateFakeUser());
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(users);

            // Write the JSON to a file
            Path path = Paths.get("users.json");
            Files.write(path, json.getBytes());

            // Create a resource from the file
            FileSystemResource resource = new FileSystemResource(path.toFile());

            response.setHeader("Content-Disposition", "attachment; filename=users.json");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/users/batch")
    public ResponseEntity<?> batchUsers(@RequestParam("file") MultipartFile file) {
        ObjectMapper mapper = new ObjectMapper();
        int totalRecords = 0;
        int successfulImports = 0;
        int failedImports = 0;

        // Ensure roles are saved in the database
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_ADMIN)));
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER)));

        try {
            User[] users = mapper.readValue(file.getInputStream(), User[].class);
            totalRecords = users.length;

            for (User user : users) {
                if (!userRepository.existsByUsername(user.getUsername()) && !userRepository.existsByEmail(user.getEmail())) {
                    user.setPassword(user.getPassword());

                    // Replace the roles in the user object with the roles from the database
                    Set<Role> savedRoles = user.getRoles().stream()
                            .map(role -> role.getName() == ERole.ROLE_ADMIN ? adminRole : userRole)
                            .collect(Collectors.toSet());
                    user.setRoles(savedRoles);

                    userRepository.save(user);
                    successfulImports++;
                } else {
                    failedImports++;
                }
            }

            Map<String, Integer> response = new HashMap<>();
            response.put("totalRecords", totalRecords);
            response.put("successfulImports", successfulImports);
            response.put("failedImports", failedImports);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/auth")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Check if a user with the provided username or email exists
        User user = userRepository.findByUsernameOrEmail(loginRequest.getUsername(), loginRequest.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate a JWT token that includes the user's email
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @GetMapping("/users/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(userDetails);
    }

    @GetMapping("/users/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (!optionalUser.isPresent()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        User user = optionalUser.get();

        if (!userDetails.getUsername().equals(username) && !userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return new ResponseEntity<>("You are not allowed to access this profile", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(user);
    }

}

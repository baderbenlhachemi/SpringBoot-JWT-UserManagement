package com.cirestechnologies.demo.controller;

import com.cirestechnologies.demo.exception.AccessDeniedException;
import com.cirestechnologies.demo.exception.InvalidPasswordException;
import com.cirestechnologies.demo.exception.UserNotFoundException;
import com.cirestechnologies.demo.model.ERole;
import com.cirestechnologies.demo.model.Role;
import com.cirestechnologies.demo.model.User;
import com.cirestechnologies.demo.payload.request.LoginRequest;
import com.cirestechnologies.demo.payload.response.JwtResponse;
import com.cirestechnologies.demo.security.jwt.JwtUtils;
import com.cirestechnologies.demo.security.services.UserDetailsImpl;
import com.cirestechnologies.demo.service.FakeDataService;
import com.cirestechnologies.demo.service.RoleService;
import com.cirestechnologies.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private FakeDataService fakeDataService;

    @GetMapping("/users/generate/{count}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FileSystemResource> generateUsers(@PathVariable int count, HttpServletResponse response) throws IOException {
        // Generate user data
        List<User> users = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            users.add(fakeDataService.generateFakeUser());
        }

        // Convert the list of users to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(users);

        // Write the JSON to a file
        Path path = Paths.get("users.json");
        Files.write(path, json.getBytes());

        // Create a resource from the file
        FileSystemResource resource = new FileSystemResource(path.toFile());

        // Set the response headers
        response.setHeader("Content-Disposition", "attachment; filename=users.json");

        // Return the file as a response
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/users/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> batchUsers(@RequestParam("file") MultipartFile file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        int totalRecords = 0;
        int successfulImports = 0;
        int failedImports = 0;

        // Ensure roles are saved in the database
        Role adminRole = roleService.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> roleService.save(new Role(ERole.ROLE_ADMIN)));
        Role userRole = roleService.findByName(ERole.ROLE_USER)
                .orElseGet(() -> roleService.save(new Role(ERole.ROLE_USER)));

        // Read the users from the file and convert them to User objects
        User[] users = mapper.readValue(file.getInputStream(), User[].class);
        totalRecords = users.length;

        // Save each user to the database
        for (User user : users) {
            if (!userService.existsByUsername(user.getUsername()) && !userService.existsByEmail(user.getEmail())) {
                user.setPassword(user.getPassword());

                // Replace the role in the user object with the role from the database
                Role savedRole = user.getRole().getName() == ERole.ROLE_ADMIN ? adminRole : userRole;
                user.setRole(savedRole);

                userService.save(user);
                successfulImports++;
            } else {
                failedImports++;
            }
        }

        // Return the response
        Map<String, Integer> response = new HashMap<>();
        response.put("totalRecords", totalRecords);
        response.put("successfulImports", successfulImports);
        response.put("failedImports", failedImports);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws UserNotFoundException, InvalidPasswordException {
        // Check if a user with the provided username or email exists
        User user = userService.findByUsernameOrEmail(loginRequest.getUsername(), loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword()));

        // Set the authentication object in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate a JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Get the roles of the authenticated user
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Return the JWT token and user details
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @GetMapping("/users/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getMyProfile() throws UserNotFoundException {
        // Fetch the user details from the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Get the UserDetailsImpl object from the authentication object
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Find the user in the database
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        return ResponseEntity.ok(user);
    }

    @GetMapping("/users/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) throws UserNotFoundException, AccessDeniedException { Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // Find the user in the database
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        // Check if the authenticated user is allowed to access this profile
        if (!userDetails.getUsername().equals(username) && !userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You are not allowed to access this profile!");
        }

        // Return the user details
        UserDetailsImpl userDetailsToReturn = UserDetailsImpl.build(user);

        return ResponseEntity.ok(userDetailsToReturn);
    }
}
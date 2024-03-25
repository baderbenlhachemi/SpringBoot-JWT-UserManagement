package ma.cirestechnologies.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.cirestechnologies.demo.models.ERole;
import ma.cirestechnologies.demo.models.Role;
import ma.cirestechnologies.demo.payload.request.LoginRequest;
import ma.cirestechnologies.demo.repository.RoleRepository;
import ma.cirestechnologies.demo.repository.UserRepository;
import ma.cirestechnologies.demo.rest.UserController;
import ma.cirestechnologies.demo.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void generateUsers_withValidCount_returnsOk() throws Exception {
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(ERole.ROLE_ADMIN)));
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(new Role(ERole.ROLE_USER)));
        when(encoder.encode(any(String.class))).thenReturn("encodedPassword");

        mockMvc.perform(get("/api/users/generate/5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getMyProfile_withAuthenticatedUser_returnsOk() throws Exception {
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void authenticateUser_withInvalidCredentials_returnsBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("test");
        loginRequest.setPassword("password");

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

}

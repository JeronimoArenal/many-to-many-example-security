package com.example.controller.security;

import com.example.security.payload.request.RegisterRequest;
import com.example.security.payload.request.RoleCreateRequest;
import com.example.security.payload.request.UserCreateRequest;
import com.example.security.payload.response.MessageResponse;
import com.example.security.model.Role;
import com.example.security.model.User;
import com.example.security.service.UserService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private String adminToken;

    private String userToken;

    // ============================================================
    // LOGIN
    // ============================================================

    @BeforeEach
    void setUp() throws Exception {

        // --------------------------------------------------------
        // Login ADMIN
        // --------------------------------------------------------

        String adminLoginJson = """
                {
                    "email": "admin@test.com",
                    "password": "123456"
                }
                """;

        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(adminLoginJson)
                )
                .andExpect(status().isOk())
                .andReturn();

        JSONObject adminJson = new JSONObject(adminLoginResult.getResponse().getContentAsString());

        adminToken = "Bearer " + adminJson.getString("token");

        // --------------------------------------------------------
        // Login USER
        // --------------------------------------------------------

        String userLoginJson = """
                {
                    "email": "user@test.com",
                    "password": "123456"
                }
                """;

        MvcResult userLoginResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userLoginJson)
                )
                .andExpect(status().isOk())
                .andReturn();

        JSONObject userJson = new JSONObject(userLoginResult.getResponse().getContentAsString());

        userToken = "Bearer " + userJson.getString("token");
    }

    // ============================================================
    // POST /api/auth/login
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/login - debe devolver un JWT")
    void shouldLoginSuccessfully() throws Exception {

        String loginJson = """
                {
                    "email": "admin@test.com",
                    "password": "123456"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // ============================================================
    // POST /api/auth/login - credenciales incorrectas
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/login - debe rechazar credenciales incorrectas")
    void shouldRejectInvalidCredentials() throws Exception {

        String loginJson = """
                {
                    "email": "admin@test.com",
                    "password": "wrong-password"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // POST /api/auth/register
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/register - debe registrar usuario")
    void shouldRegisterUserSuccessfully() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "newuser",
                "newuser@test.com",
                "password123"
        );

        mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("Usuario registrado correctamente."));

        // registerUser() devuelve void
        org.mockito.Mockito.verify(userService)
                .registerUser(any(RegisterRequest.class));
    }

    // ============================================================
    // POST /api/auth/register - validación
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/register - debe rechazar datos inválidos")
    void shouldRejectInvalidRegisterRequest() throws Exception {

        String invalidJson = """
                {
                    "username": "",
                    "email": "email-invalido",
                    "password": ""
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )
                .andExpect(status().isBadRequest());

        org.mockito.Mockito.verify(
                userService,
                org.mockito.Mockito.never()
        ).registerUser(any(RegisterRequest.class));
    }

    // ============================================================
    // POST /api/auth/roles
    // ADMIN
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/roles - ADMIN debe poder crear un rol")
    void adminShouldCreateRole() throws Exception {

        RoleCreateRequest request = new RoleCreateRequest();
        request.setCode("ROLE_TEST");
        request.setDescription("Rol de prueba");

        Role role = new Role();
        role.setCode("ROLE_TEST");
        role.setDescription("Rol de prueba");

        given(userService.createRole(
                "ROLE_TEST",
                "Rol de prueba"
        )).willReturn(role);

        mockMvc.perform(
                        post("/api/auth/roles")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("Rol 'ROLE_TEST' creado con éxito."));
    }

    // ============================================================
    // POST /api/auth/roles
    // USER -> 403
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/roles - USER debe recibir 403")
    void userShouldNotCreateRole() throws Exception {

        RoleCreateRequest request = new RoleCreateRequest();
        request.setCode("ROLE_TEST");
        request.setDescription("Rol de prueba");

        mockMvc.perform(
                        post("/api/auth/roles")
                                .header("Authorization", userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // POST /api/auth/roles
    // SIN TOKEN -> 401
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/roles - sin autenticación debe recibir 401")
    void anonymousShouldNotCreateRole() throws Exception {

        RoleCreateRequest request = new RoleCreateRequest();
        request.setCode("ROLE_TEST");
        request.setDescription("Rol de prueba");

        mockMvc.perform(
                        post("/api/auth/roles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // POST /api/auth/users
    // ADMIN
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/users - ADMIN debe poder crear usuario")
    void adminShouldCreateUser() throws Exception {

        UserCreateRequest request = new UserCreateRequest();

        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setRolesCodes(Set.of("ROLE_USER"));

        User user = new User();
        user.setUsername("newuser");
        user.setEmail("newuser@test.com");

        given(userService.createUserWithRoleCodes(
                "newuser",
                "newuser@test.com",
                "password123",
                Set.of("ROLE_USER")
        )).willReturn(user);

        mockMvc.perform(
                        post("/api/auth/users")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("Usuario 'newuser' registrado correctamente."));
    }

    // ============================================================
    // POST /api/auth/users
    // USER -> 403
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/users - USER debe recibir 403")
    void userShouldNotCreateUser() throws Exception {

        UserCreateRequest request = new UserCreateRequest();

        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setRolesCodes(Set.of("ROLE_USER"));

        mockMvc.perform(
                        post("/api/auth/users")
                                .header("Authorization", userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // POST /api/auth/users
    // SIN TOKEN -> 401
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/users - sin autenticación debe recibir 401")
    void anonymousShouldNotCreateUser() throws Exception {

        UserCreateRequest request = new UserCreateRequest();

        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setRolesCodes(Set.of("ROLE_USER"));

        mockMvc.perform(
                        post("/api/auth/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());
    }
}

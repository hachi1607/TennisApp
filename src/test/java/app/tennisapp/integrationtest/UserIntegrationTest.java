package app.tennisapp.integrationtest;

import app.tennisapp.entity.Role;
import app.tennisapp.entity.User;
import app.tennisapp.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = "ADMIN")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        savedUser = userRepository.save(User.builder()
                .email("user@test.com")
                .password("password123")
                .role(Role.USER)
                .build());
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    // getAllUsers
    @Test
    void shouldReturnAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is("user@test.com")))
                .andExpect(jsonPath("$[0].role", is("USER")))
                .andExpect(jsonPath("$[0].createdAt", notNullValue()));
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() throws Exception {
        userRepository.deleteAll();

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // getUserById
    @Test
    void shouldReturnUserByIdWhenFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is("user@test.com")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenUserIdIsInvalidType() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", "invalid"))
                .andExpect(status().isBadRequest());
    }

    // register
    @Test
    void shouldRegisterUserWhenValid() throws Exception {
        String body = """
                {
                    "email": "newuser@test.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("newuser@test.com")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void shouldReturn400WhenEmailAlreadyTaken() throws Exception {
        String body = """
                {
                    "email": "user@test.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenEmailIsInvalidOnRegister() throws Exception {
        String body = """
                {
                    "email": "notanemail",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenPasswordTooShortOnRegister() throws Exception {
        String body = """
                {
                    "email": "newuser@test.com",
                    "password": "short"
                }
                """;

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenEmailIsBlankOnRegister() throws Exception {
        String body = """
                {
                    "email": "",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // updateUser
    @Test
    void shouldUpdateUserWhenValid() throws Exception {
        String body = """
                {
                    "email": "updated@test.com",
                    "password": "newpassword123"
                }
                """;

        mockMvc.perform(patch("/api/v1/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("updated@test.com")));
    }

    @Test
    void shouldUpdateOnlyEmailWhenOnlyEmailProvided() throws Exception {
        String body = """
                {
                    "email": "updated@test.com"
                }
                """;

        mockMvc.perform(patch("/api/v1/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("updated@test.com")));
    }

    @Test
    void shouldReturn400WhenNewEmailAlreadyTaken() throws Exception {
        userRepository.save(User.builder()
                .email("taken@test.com")
                .password("password123")
                .role(Role.USER)
                .build());

        String body = """
                {
                    "email": "taken@test.com"
                }
                """;

        mockMvc.perform(patch("/api/v1/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenEmailIsInvalidOnUpdate() throws Exception {
        String body = """
                {
                    "email": "notanemail"
                }
                """;

        mockMvc.perform(patch("/api/v1/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenUserToUpdateNotFound() throws Exception {
        String body = """
                {
                    "email": "updated@test.com"
                }
                """;

        mockMvc.perform(patch("/api/v1/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // deleteUser
    @Test
    void shouldDeleteUserWhenExists() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", savedUser.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/{id}", savedUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUserToDeleteNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
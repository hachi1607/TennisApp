package app.tennisapp.integrationtest;

import app.tennisapp.entity.FavoritePlayer;
import app.tennisapp.entity.Player;
import app.tennisapp.entity.Role;
import app.tennisapp.entity.User;
import app.tennisapp.repository.FavoritePlayerRepository;
import app.tennisapp.repository.PlayerRepository;
import app.tennisapp.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FavoritePlayerIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private FavoritePlayerRepository favoritePlayerRepository;

    private User savedUser;
    private Player savedPlayer;

    @BeforeEach
    void setUp() {
        favoritePlayerRepository.deleteAll();
        playerRepository.deleteAll();
        userRepository.deleteAll();

        savedUser = userRepository.save(User.builder()
                .email("user@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build());

        savedPlayer = playerRepository.save(Player.builder()
                .externalId(100L)
                .fullName("Rafael Nadal")
                .nationality("Spain")
                .build());
    }

    @AfterEach
    void tearDown() {
        favoritePlayerRepository.deleteAll();
        playerRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void saveFavorite(User user, Player player) {
        favoritePlayerRepository.save(FavoritePlayer.builder()
                .user(user)
                .player(player)
                .build());
    }

    // getUserFavorites
    @Test
    void shouldReturnFavoritesListForUser() throws Exception {
        saveFavorite(savedUser, savedPlayer);

        mockMvc.perform(get("/api/v1/favorites")
                        .param("userId", savedUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(savedUser.getId().intValue())))
                .andExpect(jsonPath("$[0].playerId", is(savedPlayer.getId().intValue())))
                .andExpect(jsonPath("$[0].playerFullName", is("Rafael Nadal")));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoFavorites() throws Exception {
        mockMvc.perform(get("/api/v1/favorites")
                        .param("userId", savedUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturn404WhenUserNotFoundOnGetFavorites() throws Exception {
        mockMvc.perform(get("/api/v1/favorites")
                        .param("userId", "999"))
                .andExpect(status().isNotFound());
    }

    // addFavorite
    @Test
    void shouldAddFavoriteWhenValid() throws Exception {
        mockMvc.perform(post("/api/v1/favorites")
                        .param("userId", savedUser.getId().toString())
                        .param("playerId", savedPlayer.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(savedUser.getId().intValue())))
                .andExpect(jsonPath("$.playerId", is(savedPlayer.getId().intValue())))
                .andExpect(jsonPath("$.playerFullName", is("Rafael Nadal")))
                .andExpect(jsonPath("$.addedAt", notNullValue()));
    }

    @Test
    void shouldReturn400WhenFavoriteAlreadyExists() throws Exception {
        saveFavorite(savedUser, savedPlayer);

        mockMvc.perform(post("/api/v1/favorites")
                        .param("userId", savedUser.getId().toString())
                        .param("playerId", savedPlayer.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenUserNotFoundOnAddFavorite() throws Exception {
        mockMvc.perform(post("/api/v1/favorites")
                        .param("userId", "999")
                        .param("playerId", savedPlayer.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenPlayerNotFoundOnAddFavorite() throws Exception {
        mockMvc.perform(post("/api/v1/favorites")
                        .param("userId", savedUser.getId().toString())
                        .param("playerId", "999"))
                .andExpect(status().isNotFound());
    }

    // removeFavorite
    @Test
    void shouldRemoveFavoriteWhenExists() throws Exception {
        saveFavorite(savedUser, savedPlayer);

        mockMvc.perform(delete("/api/v1/favorites")
                        .param("userId", savedUser.getId().toString())
                        .param("playerId", savedPlayer.getId().toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenFavoriteNotFoundOnRemove() throws Exception {
        mockMvc.perform(delete("/api/v1/favorites")
                        .param("userId", savedUser.getId().toString())
                        .param("playerId", "999"))
                .andExpect(status().isNotFound());
    }
}
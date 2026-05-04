package app.tennisapp.service;

import app.tennisapp.dto.FavoritePlayerDto;
import app.tennisapp.entity.FavoritePlayer;
import app.tennisapp.entity.Player;
import app.tennisapp.entity.User;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.FavoritePlayerMapper;
import app.tennisapp.repository.FavoritePlayerRepository;
import app.tennisapp.repository.PlayerRepository;
import app.tennisapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoritePlayerServiceTest {
    @Mock
    private FavoritePlayerRepository favoritePlayerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private FavoritePlayerMapper favoritePlayerMapper;
    @InjectMocks
    private FavoritePlayerService favoritePlayerService;

    private static final String EMAIL = "user@test.com";
    private static final String UNKNOWN_EMAIL = "unknown@test.com";

    private User buildUser(Long id) {
        return User.builder()
                .id(id)
                .email(EMAIL)
                .build();
    }

    private Player buildPlayer(Long id) {
        return Player.builder()
                .id(id)
                .fullName("Rafael Nadal")
                .build();
    }

    private FavoritePlayer buildFavorite(User user, Player player) {
        return FavoritePlayer.builder()
                .user(user)
                .player(player)
                .build();
    }

    private FavoritePlayerDto buildFavoriteDto(Long userId, Long playerId) {
        return new FavoritePlayerDto(userId, playerId, "Rafael Nadal", null, LocalDateTime.now());
    }

    // getUserFavorites
    @Test
    void shouldGetUserFavoritesWhenUserExistsAndReturnList() {
        User user = buildUser(1L);
        Player player = buildPlayer(10L);
        FavoritePlayer favorite = buildFavorite(user, player);
        FavoritePlayerDto dto = buildFavoriteDto(1L, 10L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(favoritePlayerRepository.findByUserId(1L)).thenReturn(List.of(favorite));
        when(favoritePlayerMapper.toDto(List.of(favorite))).thenReturn(List.of(dto));

        List<FavoritePlayerDto> result = favoritePlayerService.getUserFavorites(EMAIL);

        assertNotNull(result);
        assertEquals(List.of(dto), result);
        verify(favoritePlayerRepository, times(1)).findByUserId(1L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritePlayerService.getUserFavorites(UNKNOWN_EMAIL))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(UNKNOWN_EMAIL);

        verifyNoInteractions(favoritePlayerRepository);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoFavorites() {
        User user = buildUser(1L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(favoritePlayerRepository.findByUserId(1L)).thenReturn(List.of());
        when(favoritePlayerMapper.toDto(List.of())).thenReturn(List.of());

        List<FavoritePlayerDto> result = favoritePlayerService.getUserFavorites(EMAIL);

        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findByEmail(EMAIL);
    }

    // addFavorite
    @Test
    void shouldAddFavoriteWhenValid() {
        User user = buildUser(1L);
        Player player = buildPlayer(10L);
        FavoritePlayer favorite = buildFavorite(user, player);
        FavoritePlayerDto dto = buildFavoriteDto(1L, 10L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(favoritePlayerRepository.existsByUserIdAndPlayerId(1L, 10L)).thenReturn(false);
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(favoritePlayerRepository.save(any(FavoritePlayer.class))).thenReturn(favorite);
        when(favoritePlayerMapper.toDto(favorite)).thenReturn(dto);

        FavoritePlayerDto result = favoritePlayerService.addFavorite(EMAIL, 10L);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(favoritePlayerRepository, times(1)).save(any(FavoritePlayer.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenAlreadyExists() {
        User user = buildUser(1L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(favoritePlayerRepository.existsByUserIdAndPlayerId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> favoritePlayerService.addFavorite(EMAIL, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already in favorites");

        verify(favoritePlayerRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserNotFoundOnAdd() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritePlayerService.addFavorite(UNKNOWN_EMAIL, 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(UNKNOWN_EMAIL);

        verify(favoritePlayerRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenPlayerNotFound() {
        User user = buildUser(1L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(favoritePlayerRepository.existsByUserIdAndPlayerId(1L, 99L)).thenReturn(false);
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritePlayerService.addFavorite(EMAIL, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(favoritePlayerRepository, never()).save(any());
    }

    // removeFavorite
    @Test
    void shouldRemoveFavoriteWhenExists() {
        User user = buildUser(1L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(favoritePlayerRepository.existsByUserIdAndPlayerId(1L, 10L)).thenReturn(true);

        favoritePlayerService.removeFavorite(EMAIL, 10L);

        verify(favoritePlayerRepository).deleteByUserIdAndPlayerId(1L, 10L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenNotExistsByDelete() {
        User user = buildUser(1L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(favoritePlayerRepository.existsByUserIdAndPlayerId(1L, 99L)).thenReturn(false);

        assertThatThrownBy(() -> favoritePlayerService.removeFavorite(EMAIL, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(favoritePlayerRepository, never()).deleteByUserIdAndPlayerId(any(), any());
    }
}
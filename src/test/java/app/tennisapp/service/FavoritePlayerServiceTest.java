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

    private User buildUser(Long id) {
        return User.builder()
                .id(id)
                .email("user@test.com")
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
        Long userId = 1L;
        User user = buildUser(userId);
        Player player = buildPlayer(10L);
        FavoritePlayer favorite = buildFavorite(user, player);
        FavoritePlayerDto dto = buildFavoriteDto(userId, 10L);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(favoritePlayerRepository.findByUserId(userId)).thenReturn(List.of(favorite));
        when(favoritePlayerMapper.toDto(List.of(favorite))).thenReturn(List.of(dto));

        List<FavoritePlayerDto> result = favoritePlayerService.getUserFavorites(userId);

        assertNotNull(result);
        assertEquals(List.of(dto), result);
        verify(favoritePlayerRepository, times(1)).findByUserId(userId);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenIdNotExist() {
        Long givenId = 99L;

        when(userRepository.existsById(givenId)).thenReturn(false);

        assertThatThrownBy(() -> favoritePlayerService.getUserFavorites(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verifyNoInteractions(favoritePlayerRepository);
        verify(userRepository, times(1)).existsById(99L);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoFavorite() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(favoritePlayerRepository.findByUserId(1L)).thenReturn(List.of());
        when(favoritePlayerMapper.toDto(List.of())).thenReturn(List.of());

        List<FavoritePlayerDto> result = favoritePlayerService.getUserFavorites(1L);

        assertThat(result).isEmpty();
        assertNotNull(result);
        assertEquals(List.of(), result);
        verify(userRepository, times(1)).existsById(1L);
    }

    // addFavorite
    @Test
    void shouldAddFavoriteWhenValid() {
        Long userId = 1L;
        Long playerId = 10L;
        User user = buildUser(userId);
        Player player = buildPlayer(playerId);
        FavoritePlayer favorite = buildFavorite(user, player);
        FavoritePlayerDto dto = buildFavoriteDto(userId, playerId);

        when(favoritePlayerRepository.existsByUserIdAndPlayerId(userId, playerId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(favoritePlayerRepository.save(any(FavoritePlayer.class))).thenReturn(favorite);
        when(favoritePlayerMapper.toDto(favorite)).thenReturn(dto);

        FavoritePlayerDto result = favoritePlayerService.addFavorite(userId, playerId);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(favoritePlayerRepository, times(1)).save(any(FavoritePlayer.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenAlreadyExists() {
        when(favoritePlayerRepository.existsByUserIdAndPlayerId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> favoritePlayerService.addFavorite(1L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already in favorites");

        verify(favoritePlayerRepository, never()).save(any());
        verify(favoritePlayerRepository, times(1)).existsByUserIdAndPlayerId(1L, 10L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        when(favoritePlayerRepository.existsByUserIdAndPlayerId(99L, 10L)).thenReturn(false);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritePlayerService.addFavorite(99L, 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(favoritePlayerRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenPlayerNotFound() {
        Long userId = 1L;
        when(favoritePlayerRepository.existsByUserIdAndPlayerId(userId, 99L)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser(userId)));
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritePlayerService.addFavorite(userId, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(favoritePlayerRepository, never()).save(any());
    }

    // removeFavorite
    @Test
    void shouldRemoveFavoriteWhenExists() {
        when(favoritePlayerRepository.existsByUserIdAndPlayerId(1L, 10L)).thenReturn(true);

        favoritePlayerService.removeFavorite(1L, 10L);

        verify(favoritePlayerRepository).deleteByUserIdAndPlayerId(1L, 10L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenNotExistsByDelete() {
        when(favoritePlayerRepository.existsByUserIdAndPlayerId(1L, 99L)).thenReturn(false);

        assertThatThrownBy(() -> favoritePlayerService.removeFavorite(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1")
                .hasMessageContaining("99");

        verify(favoritePlayerRepository, never()).deleteByUserIdAndPlayerId(any(), any());
    }
}
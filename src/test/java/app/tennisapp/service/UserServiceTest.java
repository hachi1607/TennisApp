package app.tennisapp.service;

import app.tennisapp.command.RegisterCommand;
import app.tennisapp.command.UpdateUserCommand;
import app.tennisapp.dto.UserDto;
import app.tennisapp.entity.Role;
import app.tennisapp.entity.User;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.UserMapper;
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
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserService userService;

    private User buildUser(Long id) {
        return User.builder()
                .id(id)
                .email("user@test.com")
                .password("password123")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private UserDto buildUserDto(Long id) {
        return new UserDto(id, "user@test.com", Role.USER, LocalDateTime.now());
    }

    private RegisterCommand buildRegisterCommand() {
        return new RegisterCommand("user@test.com", "password123");
    }

    private UpdateUserCommand buildUpdateCommand() {
        return new UpdateUserCommand("updated@test.com", "newpassword123");
    }

    // getAllUsers
    @Test
    void shouldReturnAllUsers() {
        User user = buildUser(1L);
        UserDto dto = buildUserDto(1L);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDto(List.of(user))).thenReturn(List.of(dto));

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(List.of(dto), result);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());
        when(userMapper.toDto(List.of())).thenReturn(List.of());

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).isEmpty();
    }

    // getUserById
    @Test
    void shouldReturnUserWhenFound() {
        User user = buildUser(1L);
        UserDto dto = buildUserDto(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(userMapper, never()).toDto(any(User.class));
    }

    // register
    @Test
    void shouldRegisterUserWhenEmailNotTaken() {
        RegisterCommand command = buildRegisterCommand();
        User user = buildUser(1L);
        UserDto dto = buildUserDto(1L);

        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(userMapper.toEntity(command, command.password())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = userService.register(command);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenEmailAlreadyTaken() {
        RegisterCommand command = buildRegisterCommand();

        when(userRepository.existsByEmail(command.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(command.email());

        verify(userRepository, never()).save(any());
    }

    // updateUser
    @Test
    void shouldUpdateUserWhenValid() {
        UpdateUserCommand command = buildUpdateCommand();
        User user = buildUser(1L);
        User updated = user.toBuilder().email("updated@test.com").password("newpassword123").build();
        UserDto dto = new UserDto(1L, "updated@test.com", Role.USER, LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updated);
        when(userMapper.toDto(updated)).thenReturn(dto);

        UserDto result = userService.updateUser(1L, command);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldUpdateOnlyPasswordWhenEmailIsNull() {
        UpdateUserCommand command = new UpdateUserCommand(null, "newpassword123");
        User user = buildUser(1L);
        User updated = user.toBuilder().password("newpassword123").build();
        UserDto dto = buildUserDto(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updated);
        when(userMapper.toDto(updated)).thenReturn(dto);

        UserDto result = userService.updateUser(1L, command);

        assertNotNull(result);
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenNewEmailAlreadyTaken() {
        UpdateUserCommand command = buildUpdateCommand();
        User user = buildUser(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("updated@test.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserToUpdateNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, buildUpdateCommand()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository, never()).save(any());
    }

    // deleteUser
    @Test
    void shouldDeleteUserWhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserToDeleteNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository, never()).deleteById(any());
    }
}
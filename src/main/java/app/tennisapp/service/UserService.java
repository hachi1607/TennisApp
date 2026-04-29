package app.tennisapp.service;

import app.tennisapp.command.RegisterCommand;
import app.tennisapp.command.UpdateUserCommand;
import app.tennisapp.dto.UserDto;
import app.tennisapp.entity.User;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.UserMapper;
import app.tennisapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto register(RegisterCommand command) {
        log.info("Registering user, email='{}'", command.email());
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalStateException("Email already in use: " + command.email());
        }
        User saved = userRepository.save(userMapper.toEntity(command, command.password()));
        log.info("User registered, id={}", saved.getId());
        return userMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userMapper.toDto(userRepository.findAll());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserCommand command) {
        log.info("Updating user id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        if (command.email() != null && !command.email().equals(user.getEmail())
                && userRepository.existsByEmail(command.email())) {
            throw new IllegalStateException("Email already in use: " + command.email());
        }

        User.UserBuilder builder = user.toBuilder();
        if (command.email() != null) {
            builder.email(command.email());
        }
        if (command.password() != null) {
            builder.password(command.password());
        }

        return userMapper.toDto(userRepository.save(builder.build()));
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user id={}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted id={}", id);
    }
}
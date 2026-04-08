package app.tennisapp.mapper;

import app.tennisapp.command.RegisterCommand;
import app.tennisapp.dto.UserDto;
import app.tennisapp.entity.Role;
import app.tennisapp.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {
    public User toEntity(RegisterCommand command, String encodedPassword) {
        return User.builder()
                .email(command.email())
                .password(encodedPassword)
                .role(Role.USER)
                .build();
    }

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    public List<UserDto> toDto(List<User> users) {
        return users.stream()
                .map(this::toDto)
                .toList();
    }
}
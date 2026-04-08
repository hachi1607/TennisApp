package app.tennisapp.mapper;

import app.tennisapp.dto.FavoritePlayerDto;
import app.tennisapp.entity.FavoritePlayer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FavoritePlayerMapper {
    public FavoritePlayerDto toDto(FavoritePlayer favorite) {
        return new FavoritePlayerDto(
                favorite.getUser().getId(),
                favorite.getPlayer().getId(),
                favorite.getPlayer().getFullName(),
                favorite.getPlayer().getImageUrl(),
                favorite.getAddedAt()
        );
    }

    public List<FavoritePlayerDto> toDto(List<FavoritePlayer> favorites) {
        return favorites.stream()
                .map(this::toDto)
                .toList();
    }
}
package app.tennisapp.mapper;

import app.tennisapp.command.CreateNewsCommand;
import app.tennisapp.dto.NewsDto;
import app.tennisapp.entity.News;
import app.tennisapp.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewsMapper {
    public News toEntity(CreateNewsCommand command, User author) {
        return News.builder()
                .author(author)
                .title(command.title())
                .content(command.content())
                .imageUrl(command.imageUrl())
                .build();
    }

    public NewsDto toDto(News news) {
        return new NewsDto(
                news.getId(),
                news.getAuthor().getId(),
                news.getAuthor().getEmail(),
                news.getTitle(),
                news.getContent(),
                news.getImageUrl(),
                news.getPublishedAt()
        );
    }

    public List<NewsDto> toDto(List<News> newsList) {
        return newsList.stream()
                .map(this::toDto)
                .toList();
    }
}
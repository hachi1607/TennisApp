package app.tennisapp.service;

import app.tennisapp.command.CreateNewsCommand;
import app.tennisapp.command.UpdateNewsCommand;
import app.tennisapp.dto.NewsDto;
import app.tennisapp.entity.News;
import app.tennisapp.entity.User;
import app.tennisapp.exception.ResourceNotFoundException;
import app.tennisapp.mapper.NewsMapper;
import app.tennisapp.repository.NewsRepository;
import app.tennisapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
class NewsServiceTest {
    @Mock
    private NewsRepository newsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NewsMapper newsMapper;
    @InjectMocks
    private NewsService newsService;

    private User buildUser() {
        return User.builder().id(1L).email("author@test.com").build();
    }

    private News buildNews(User author) {
        return News.builder()
                .id(1L)
                .author(author)
                .title("Test Title")
                .content("Test Content")
                .imageUrl("https://example.com/image.jpg")
                .build();
    }

    private NewsDto buildNewsDto() {
        return new NewsDto(1L, 1L, "author@test.com", "Test Title",
                "Test Content", "https://example.com/image.jpg", LocalDateTime.now());
    }

    private CreateNewsCommand buildCreateCommand() {
        return new CreateNewsCommand("Test Title", "Test Content", "https://example.com/image.jpg");
    }

    private UpdateNewsCommand buildUpdateCommand() {
        return new UpdateNewsCommand("Updated Title", "Updated Content", "https://example.com/new.jpg");
    }

    // getAllNewsPaged
    @Test
    void shouldReturnPageOfNewsPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        User author = buildUser();
        News news = buildNews(author);
        NewsDto dto = buildNewsDto();
        Page<News> newsPage = new PageImpl<>(List.of(news), pageable, 1);
        Page<NewsDto> newsDtoPage = new PageImpl<>(List.of(dto), pageable, 1);

        when(newsRepository.findAllPaged(pageable)).thenReturn(newsPage);
        when(newsMapper.toDto(news)).thenReturn(dto);

        Page<NewsDto> result = newsService.getAllNewsPaged(pageable);

        assertNotNull(result);
        assertEquals(newsDtoPage, result);
        verify(newsRepository, times(1)).findAllPaged(pageable);
    }

    @Test
    void shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(newsRepository.findAllPaged(pageable)).thenReturn(Page.empty());

        Page<NewsDto> result = newsService.getAllNewsPaged(pageable);

        assertThat(result.getContent()).isEmpty();
    }

    // getNewsById
    @Test
    void shouldReturnDto() {
        User author = buildUser();
        News news = buildNews(author);
        NewsDto dto = buildNewsDto();

        when(newsRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(news));
        when(newsMapper.toDto(news)).thenReturn(dto);

        NewsDto result = newsService.getNewsById(1L);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(newsRepository, times(1)).findByIdWithAuthor(1L);
    }

    @Test
    void shouldThrowResourceNotFoundException() {
        when(newsRepository.findByIdWithAuthor(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.getNewsById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(newsMapper, never()).toDto(any(News.class));
    }

    // searchNews
    @Test
    void shouldReturnMatchingNews() {
        User author = buildUser();
        News news = buildNews(author);
        NewsDto dto = buildNewsDto();

        when(newsRepository.searchByKeyword("wimbledon")).thenReturn(List.of(news));
        when(newsMapper.toDto(List.of(news))).thenReturn(List.of(dto));

        List<NewsDto> result = newsService.searchNews("wimbledon");

        assertNotNull(result);
        assertEquals(List.of(dto), result);
        verify(newsRepository, times(1)).searchByKeyword("wimbledon");
    }

    @Test
    void shouldReturnEmptyList() {
        when(newsRepository.searchByKeyword("xyz")).thenReturn(List.of());
        when(newsMapper.toDto(List.of())).thenReturn(List.of());

        List<NewsDto> result = newsService.searchNews("xyz");

        assertThat(result).isEmpty();
    }

    // createNews
    @Test
    void shouldReturnCreatedNewsDto() {
        User author = buildUser();
        CreateNewsCommand command = buildCreateCommand();
        News news = buildNews(author);
        NewsDto dto = buildNewsDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(newsMapper.toEntity(command, author)).thenReturn(news);
        when(newsRepository.save(news)).thenReturn(news);
        when(newsMapper.toDto(news)).thenReturn(dto);

        NewsDto result = newsService.createNews(1L, command);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(userRepository, times(1)).findById(1L);
        verify(newsRepository, times(1)).save(news);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAuthorNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.createNews(99L, buildCreateCommand()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(newsRepository, never()).save(any());
    }

    // updateNews
    @Test
    void shouldReturnUpdatedDto() {
        User author = buildUser();
        News existing = buildNews(author);
        UpdateNewsCommand command = buildUpdateCommand();
        News updated = existing.toBuilder()
                .title(command.title())
                .content(command.content())
                .imageUrl(command.imageUrl())
                .build();
        NewsDto dto = new NewsDto(1L, 1L, "author@test.com", "Updated Title",
                "Updated Content", "https://example.com/new.jpg", LocalDateTime.now());

        when(newsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(newsRepository.save(any(News.class))).thenReturn(updated);
        when(newsMapper.toDto(updated)).thenReturn(dto);

        NewsDto result = newsService.updateNews(1L, command);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(newsRepository, times(1)).save(any(News.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenNotExists() {
        when(newsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.updateNews(99L, buildUpdateCommand()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(newsRepository, never()).save(any());
    }

    // patchNews
    @Test
    void shouldUpdateOnlyTitle() {
        User author = buildUser();
        News existing = buildNews(author);
        UpdateNewsCommand command = new UpdateNewsCommand("New Title", null, null);
        News patched = existing.toBuilder().title("New Title").build();
        NewsDto dto = new NewsDto(1L, 1L, "author@test.com", "New Title",
                "Test Content", "https://example.com/image.jpg", LocalDateTime.now());

        when(newsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(newsRepository.save(any(News.class))).thenReturn(patched);
        when(newsMapper.toDto(patched)).thenReturn(dto);

        NewsDto result = newsService.patchNews(1L, command);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(newsRepository, times(1)).save(any(News.class));
    }

    @Test
    void shouldUpdateAllFieldsWhenAllFieldsProvided() {
        User author = buildUser();
        News existing = buildNews(author);
        UpdateNewsCommand command = buildUpdateCommand();
        News patched = existing.toBuilder()
                .title(command.title())
                .content(command.content())
                .imageUrl(command.imageUrl())
                .build();
        NewsDto dto = new NewsDto(1L, 1L, "author@test.com", "Updated Title",
                "Updated Content", "https://example.com/new.jpg", LocalDateTime.now());

        when(newsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(newsRepository.save(any(News.class))).thenReturn(patched);
        when(newsMapper.toDto(patched)).thenReturn(dto);

        NewsDto result = newsService.patchNews(1L, command);

        assertNotNull(result);
        assertEquals(dto, result);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenNewsToPatchNotExists() {
        when(newsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.patchNews(99L, buildUpdateCommand()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(newsRepository, never()).save(any());
    }

    // deleteNewsById
    @Test
    void shouldDeleteSuccessfully() {
        when(newsRepository.existsById(1L)).thenReturn(true);

        newsService.deleteNewsById(1L);

        verify(newsRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenNewsToDeleteNotExists() {
        when(newsRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> newsService.deleteNewsById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(newsRepository, never()).deleteById(any());
    }
}
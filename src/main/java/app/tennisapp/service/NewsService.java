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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final NewsMapper newsMapper;

//    public List<NewsDto> getAllNews() {
//        return newsMapper.toDto(newsRepository.findAllByOrderByPublishedAtDesc());
//    }

    public Page<NewsDto> getAllNewsPaged(Pageable pageable) {
        log.debug("Fetching all news, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return newsRepository.findAllPaged(pageable)
                .map(newsMapper::toDto);
    }

    public NewsDto getNewsById(Long id) {
        Optional<News> news = newsRepository.findByIdWithAuthor(id);
        return newsMapper.toDto(news.orElseThrow(() -> new ResourceNotFoundException("News not found: " + id)));
    }

    public List<NewsDto> searchNews(String keyword) {
        return newsMapper.toDto(newsRepository.searchByKeyword(keyword));
    }

    @Transactional
    public NewsDto createNews(Long authorId, CreateNewsCommand command) {
        log.info("Creating news, authorId={}, title='{}'", authorId, command.title());
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authorId));

        News news = newsMapper.toEntity(command, author);
        News saved = newsRepository.save(news);
        log.info("News created, id={}", saved.getId());
        return newsMapper.toDto(saved);
    }

    @Transactional
    public NewsDto updateNews(Long id, UpdateNewsCommand command) {
        News news = getNewsEntityById(id);

        News updated = news.toBuilder()
                .title(command.title())
                .content(command.content())
                .imageUrl(command.imageUrl())
                .build();

        return newsMapper.toDto(newsRepository.save(updated));
    }

    @Transactional
    public NewsDto patchNews(Long id, UpdateNewsCommand command) {
        News news = getNewsEntityById(id);
        News.NewsBuilder builder = news.toBuilder();

        if (command.title() != null) builder.title(command.title());
        if (command.content() != null) builder.content(command.content());
        if (command.imageUrl() != null) builder.imageUrl(command.imageUrl());

        return newsMapper.toDto(newsRepository.save(builder.build()));
    }

    @Transactional
    public void deleteNewsById(Long id) {
        log.info("Deleting news id={}", id);
        if (!newsRepository.existsById(id)) {
            log.warn("News not found: id={}", id);
            throw new ResourceNotFoundException("News not found: " + id);
        }
        newsRepository.deleteById(id);
        log.info("News deleted id={}", id);
    }

    private News getNewsEntityById(Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found: " + id));
    }
}
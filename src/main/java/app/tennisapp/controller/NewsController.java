package app.tennisapp.controller;

import app.tennisapp.command.CreateNewsCommand;
import app.tennisapp.command.UpdateNewsCommand;
import app.tennisapp.dto.NewsDto;
import app.tennisapp.service.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/news")
@RestController
public class NewsController {
    private final NewsService newsService;

    @PostMapping
    public ResponseEntity<NewsDto> createNews(
            @RequestParam Long authorId,
            @Valid @RequestBody CreateNewsCommand command
    ) {
        return ResponseEntity.status(201).body(newsService.createNews(authorId, command));
    }

    @GetMapping
    public ResponseEntity<Page<NewsDto>> getAllNewsPaged(
    @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC)
    Pageable pageable
    ) {
        return ResponseEntity.ok().body(newsService.getAllNewsPaged(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsDto> getNewsById(@PathVariable Long id) {
        return ResponseEntity.ok().body(newsService.getNewsById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<NewsDto>> searchNews(@RequestParam String keyword) {
        return ResponseEntity.ok().body(newsService.searchNews(keyword));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NewsDto> updateNews(@PathVariable Long id, @Valid @RequestBody UpdateNewsCommand command) {
        return ResponseEntity.ok().body(newsService.updateNews(id, command));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NewsDto> patchNews(@PathVariable Long id, @RequestBody UpdateNewsCommand command) {
        return ResponseEntity.ok().body(newsService.patchNews(id, command));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNewsById(@PathVariable Long id) {
        newsService.deleteNewsById(id);
        return ResponseEntity.noContent().build();
    }
}
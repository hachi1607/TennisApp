package app.tennisapp.controller;

import app.tennisapp.command.CreateNewsCommand;
import app.tennisapp.command.UpdateNewsCommand;
import app.tennisapp.dto.NewsDto;
import app.tennisapp.service.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/news")
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
    public ResponseEntity<List<NewsDto>> getAllNews() { // getAll paginacja!
        return ResponseEntity.ok().body(newsService.getAllNews());
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
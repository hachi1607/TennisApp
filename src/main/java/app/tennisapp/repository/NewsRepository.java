package app.tennisapp.repository;

import app.tennisapp.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findAllByOrderByPublishedAtDesc();

    List<News> findByAuthorId(Long authorId);

    List<News> findByContentContainingIgnoreCaseOrTitleContainingIgnoreCase(String contentKeyword, String titleKeyword);
}
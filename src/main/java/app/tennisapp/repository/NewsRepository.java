package app.tennisapp.repository;

import app.tennisapp.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {

    @Query("SELECT n FROM News n LEFT JOIN FETCH n.author ORDER BY n.publishedAt DESC")
    List<News> findAllByOrderByPublishedAtDesc();

    @Query(
            value = "SELECT n FROM News n LEFT JOIN FETCH n.author ORDER BY n.publishedAt DESC",
            countQuery = "SELECT COUNT(n) FROM News n"
    )
    Page<News> findAllPaged(Pageable pageable);

    @Query("SELECT n FROM News n LEFT JOIN FETCH n.author WHERE n.id = :id")
    Optional<News> findByIdWithAuthor(@Param("id") Long id);

    @Query("SELECT n FROM News n LEFT JOIN FETCH n.author WHERE LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<News> searchByKeyword(@Param("keyword") String keyword);
}
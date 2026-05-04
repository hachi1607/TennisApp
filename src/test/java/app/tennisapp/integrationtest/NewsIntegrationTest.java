package app.tennisapp.integrationtest;

import app.tennisapp.entity.News;
import app.tennisapp.entity.Role;
import app.tennisapp.entity.User;
import app.tennisapp.repository.NewsRepository;
import app.tennisapp.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = "ADMIN")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NewsIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private UserRepository userRepository;

    private User savedAuthor;
    private News savedNews;

    @BeforeEach
    void setUp() {
        newsRepository.deleteAll();
        userRepository.deleteAll();

        savedAuthor = userRepository.save(User.builder()
                .email("author@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build());

        savedNews = newsRepository.save(News.builder()
                .author(savedAuthor)
                .title("Wimbledon 2025 Preview")
                .content("This year's Wimbledon promises to be exciting.")
                .imageUrl("https://example.com/wimbledon.jpg")
                .build());
    }

    @AfterEach
    void tearDown() {
        newsRepository.deleteAll();
        userRepository.deleteAll();
    }

    private News buildAndSaveNews(User author, String title, String content) {
        return newsRepository.save(News.builder()
                .author(author)
                .title(title)
                .content(content)
                .build());
    }

    // createNews
    @Test
    void shouldCreateNewsWhenValid() throws Exception {
        String body = """
                {
                    "title": "US Open Preview",
                    "content": "The US Open is coming to New York.",
                    "imageUrl": "https://example.com/usopen.jpg"
                }
                """;

        mockMvc.perform(post("/api/v1/news")
                        .param("authorId", savedAuthor.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("US Open Preview")))
                .andExpect(jsonPath("$.content", is("The US Open is coming to New York.")))
                .andExpect(jsonPath("$.authorEmail", is("author@test.com")))
                .andExpect(jsonPath("$.publishedAt", notNullValue()));
    }

    @Test
    void shouldReturn404WhenAuthorNotFoundOnCreate() throws Exception {
        String body = """
                {
                    "title": "US Open Preview",
                    "content": "The US Open is coming to New York!!!!!"
                }
                """;

        mockMvc.perform(post("/api/v1/news")
                        .param("authorId", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenTitleIsBlankOnCreate() throws Exception {
        String body = """
                {
                    "title": "",
                    "content": "Some content hereeeeee"
                }
                """;

        mockMvc.perform(post("/api/v1/news")
                        .param("authorId", savedAuthor.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenContentIsBlankOnCreate() throws Exception {
        String body = """
                {
                    "title": "Some title",
                    "content": ""
                }
                """;

        mockMvc.perform(post("/api/v1/news")
                        .param("authorId", savedAuthor.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // getAllNewsPaged
    @Test
    void shouldReturnPagedNewsWithNoFilters() throws Exception {
        mockMvc.perform(get("/api/v1/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Wimbledon 2025 Preview")))
                .andExpect(jsonPath("$.content[0].authorEmail", is("author@test.com")))
                .andExpect(jsonPath("$.content[0].publishedAt", notNullValue()));
    }

    @Test
    void shouldReturnEmptyPageWhenNoNews() throws Exception {
        newsRepository.deleteAll();

        mockMvc.perform(get("/api/v1/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void shouldReturnNewsOrderedByPublishedAtDesc() throws Exception {
        buildAndSaveNews(savedAuthor, "Roland Garros Preview", "Clay season incoming.");

        mockMvc.perform(get("/api/v1/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title", is("Roland Garros Preview")))
                .andExpect(jsonPath("$.content[1].title", is("Wimbledon 2025 Preview")));
    }

    // getNewsById
    @Test
    void shouldReturnNewsByIdWhenFound() throws Exception {
        mockMvc.perform(get("/api/v1/news/{id}", savedNews.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedNews.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Wimbledon 2025 Preview")))
                .andExpect(jsonPath("$.content", is("This year's Wimbledon promises to be exciting.")))
                .andExpect(jsonPath("$.authorEmail", is("author@test.com")))
                .andExpect(jsonPath("$.imageUrl", is("https://example.com/wimbledon.jpg")));
    }

    @Test
    void shouldReturn404WhenNewsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/news/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenNewsIdIsInvalidType() throws Exception {
        mockMvc.perform(get("/api/v1/news/{id}", "invalid"))
                .andExpect(status().isBadRequest());
    }

    // searchNews
    @Test
    void shouldReturnMatchingNewsOnSearch() throws Exception {
        buildAndSaveNews(savedAuthor, "Roland Garros Preview", "Clay season incoming.");

        mockMvc.perform(get("/api/v1/news/search")
                        .param("keyword", "wimbledon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Wimbledon 2025 Preview")));
    }

    @Test
    void shouldReturnEmptyListWhenNoNewsMatchSearch() throws Exception {
        mockMvc.perform(get("/api/v1/news/search")
                        .param("keyword", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldSearchNewsInContentAsWell() throws Exception {
        mockMvc.perform(get("/api/v1/news/search")
                        .param("keyword", "exciting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Wimbledon 2025 Preview")));
    }

    // updateNews
    @Test
    void shouldUpdateNewsWhenValid() throws Exception {
        String body = """
                {
                    "title": "Updated Title",
                    "content": "Updated content.",
                    "imageUrl": "https://example.com/updated.jpg"
                }
                """;

        mockMvc.perform(put("/api/v1/news/{id}", savedNews.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.content", is("Updated content.")))
                .andExpect(jsonPath("$.imageUrl", is("https://example.com/updated.jpg")));
    }

    @Test
    void shouldReturn404WhenNewsNotFoundOnUpdate() throws Exception {
        String body = """
                {
                    "title": "Updated Title",
                    "content": "Updated content."
                }
                """;

        mockMvc.perform(put("/api/v1/news/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenTitleExceedsMaxLengthOnUpdate() throws Exception {
        String body = """
                {
                    "title": "%s",
                    "content": "Some content."
                }
                """.formatted("A".repeat(256));

        mockMvc.perform(put("/api/v1/news/{id}", savedNews.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // patchNews
    @Test
    void shouldPatchOnlyTitleWhenOnlyTitleProvided() throws Exception {
        String body = """
                {
                    "title": "Patched Title"
                }
                """;

        mockMvc.perform(patch("/api/v1/news/{id}", savedNews.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Patched Title")))
                .andExpect(jsonPath("$.content", is("This year's Wimbledon promises to be exciting.")));
    }

    @Test
    void shouldPatchAllFieldsWhenAllProvided() throws Exception {
        String body = """
                {
                    "title": "Patched Title",
                    "content": "Patched content.",
                    "imageUrl": "https://example.com/patched.jpg"
                }
                """;

        mockMvc.perform(patch("/api/v1/news/{id}", savedNews.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Patched Title")))
                .andExpect(jsonPath("$.content", is("Patched content.")))
                .andExpect(jsonPath("$.imageUrl", is("https://example.com/patched.jpg")));
    }

    @Test
    void shouldReturn404WhenNewsNotFoundOnPatch() throws Exception {
        String body = """
                {
                    "title": "Patched Title"
                }
                """;

        mockMvc.perform(patch("/api/v1/news/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // deleteNewsById
    @Test
    void shouldDeleteNewsWhenExists() throws Exception {
        mockMvc.perform(delete("/api/v1/news/{id}", savedNews.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/news/{id}", savedNews.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenNewsNotFoundOnDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/news/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
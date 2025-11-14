package dev.devrunner.api.techblog;

import dev.devrunner.api.techblog.dto.TechBlogRead;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TechCategory;
import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.model.techblog.TechBlogIdentity;
import dev.devrunner.service.techblog.TechBlogReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * TechBlogApiController 테스트
 *
 * @ExtendWith(MockitoExtension.class) 사용
 * MockMvc 없이 Controller를 직접 호출하여 테스트
 */
@ExtendWith(MockitoExtension.class)
class TechBlogApiControllerTest {

    @Mock
    private TechBlogReader techBlogReader;

    @InjectMocks
    private TechBlogApiController controller;

    // ========== getTechBlog 테스트 ==========

    @Test
    void getTechBlog_existingId_returnsOkWithTechBlog() {
        // given
        Long techBlogId = 1L;
        TechBlog techBlog = createSampleTechBlog(techBlogId);

        when(techBlogReader.read(new TechBlogIdentity(techBlogId))).thenReturn(techBlog);

        // when
        ResponseEntity<TechBlogRead> response = controller.getTechBlog(techBlogId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTechBlogId()).isEqualTo(techBlogId);
        assertThat(response.getBody().getCompany()).isEqualTo("Test Company");
        assertThat(response.getBody().getTitle()).isEqualTo("Mastering Spring Boot");
        assertThat(response.getBody().getUrl()).isEqualTo("https://test.com/blog/1");
        assertThat(response.getBody().getOneLiner()).isEqualTo("Great tutorial for Spring Boot");
        verify(techBlogReader).read(new TechBlogIdentity(techBlogId));
    }

    @Test
    void getTechBlog_nonExistingId_throwsException() {
        // given
        Long techBlogId = 999L;

        when(techBlogReader.read(new TechBlogIdentity(techBlogId)))
                .thenThrow(new RuntimeException("TechBlog not found"));

        // when & then
        try {
            controller.getTechBlog(techBlogId);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("TechBlog not found");
        }

        verify(techBlogReader).read(new TechBlogIdentity(techBlogId));
    }

    // ========== 헬퍼 메서드 ==========

    private TechBlog createSampleTechBlog(Long techBlogId) {
        return new TechBlog(
                techBlogId,                              // techBlogId
                "https://test.com/blog/" + techBlogId,  // url
                "Test Company",                         // company
                "Mastering Spring Boot",                // title
                "Great tutorial for Spring Boot",       // oneLiner
                "Summary of Spring Boot tutorial",      // summary
                "스프링 부트 튜토리얼 테스트",
                "Test markdown body content",           // markdownBody
                "https://test.com/thumb.jpg",           // thumbnailUrl
                List.of(TechCategory.JAVA, TechCategory.SPRING), // techCategories
                "https://original.com/blog",            // originalUrl
                Popularity.empty(),                     // popularity
                false,                                  // isDeleted
                Instant.now(),                          // createdAt
                Instant.now()                           // updatedAt
        );
    }
}

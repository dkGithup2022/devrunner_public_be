package dev.devrunner.service.techblog.impl;

import dev.devrunner.exception.techblog.TechBlogNotFoundException;
import dev.devrunner.infra.techblog.repository.TechBlogRepository;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.model.techblog.TechBlogIdentity;
import dev.devrunner.service.techblog.view.TechBlogViewMemory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultTechBlogReaderTest {

    @Mock
    private TechBlogRepository techBlogRepository;

    @Mock
    private TechBlogViewMemory techBlogViewMemory;

    @InjectMocks
    private DefaultTechBlogReader techBlogReader;

    private final TechBlog sampleBlog = new TechBlog(
            1L,                              // techBlogId
            "https://example.com/blog/1",    // url
            "test company",                  // company
            "test title",                    // title
            null,                            // oneLiner
            null,                            // summary
            null,
            "test markdown body",            // markdownBody
            "https://example.com/thumb.jpg", // thumbnailUrl
            List.of(),                       // techCategories
            "https://original.com/post",     // originalUrl
            Popularity.empty(),              // popularity
            false,                           // isDeleted
            Instant.now(),                   // createdAt
            Instant.now()                    // updatedAt
    );

    private final TechBlogIdentity testIdentity = new TechBlogIdentity(1L);

    @Test
    void read_existingId_returnsBlogAndIncrementsViewCount() {
        // given
        when(techBlogRepository.findById(testIdentity))
                .thenReturn(Optional.of(sampleBlog));

        // when
        TechBlog result = techBlogReader.read(testIdentity);

        // then
        assertNotNull(result);
        assertEquals(sampleBlog.getTechBlogId(), result.getTechBlogId());
        assertEquals(sampleBlog.getTitle(), result.getTitle());
        verify(techBlogRepository).findById(testIdentity);
        verify(techBlogViewMemory).countUp(1L);
    }

    @Test
    void read_nonExistingId_throwsTechBlogNotFoundException() {
        // given
        when(techBlogRepository.findById(testIdentity))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(TechBlogNotFoundException.class, () ->
                techBlogReader.read(testIdentity)
        );
        verify(techBlogRepository).findById(testIdentity);
        verify(techBlogViewMemory, never()).countUp(any());
    }

    @Test
    void getById_existingId_returnsBlogWithoutViewCount() {
        // given
        when(techBlogRepository.findById(testIdentity))
                .thenReturn(Optional.of(sampleBlog));

        // when
        TechBlog result = techBlogReader.getById(testIdentity);

        // then
        assertNotNull(result);
        assertEquals(sampleBlog.getTechBlogId(), result.getTechBlogId());
        assertEquals(sampleBlog.getTitle(), result.getTitle());
        verify(techBlogRepository).findById(testIdentity);
        verify(techBlogViewMemory, never()).countUp(any());
    }

    @Test
    void getById_nonExistingId_throwsTechBlogNotFoundException() {
        // given
        when(techBlogRepository.findById(testIdentity))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(TechBlogNotFoundException.class, () ->
                techBlogReader.getById(testIdentity)
        );
        verify(techBlogRepository).findById(testIdentity);
    }

    @Test
    void getByIds_withExistingIds_returnsList() {
        // given
        TechBlogIdentity identity1 = new TechBlogIdentity(1L);
        TechBlogIdentity identity2 = new TechBlogIdentity(2L);
        List<TechBlogIdentity> identities = List.of(identity1, identity2);

        TechBlog blog2 = new TechBlog(
                2L, "https://example.com/blog/2", "test company 2", "test title 2",
                null, null, null, "test markdown body 2", "https://example.com/thumb2.jpg",
                List.of(), "https://original.com/post2", Popularity.empty(),
                false, Instant.now(), Instant.now()
        );

        List<TechBlog> blogs = List.of(sampleBlog, blog2);
        when(techBlogRepository.findByIdsIn(identities))
                .thenReturn(blogs);

        // when
        List<TechBlog> result = techBlogReader.getByIds(identities);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(techBlogRepository).findByIdsIn(identities);
    }

    @Test
    void getByIds_withEmptyIds_returnsEmptyList() {
        // given
        List<TechBlogIdentity> emptyIdentities = List.of();
        when(techBlogRepository.findByIdsIn(emptyIdentities))
                .thenReturn(List.of());

        // when
        List<TechBlog> result = techBlogReader.getByIds(emptyIdentities);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(techBlogRepository).findByIdsIn(emptyIdentities);
    }

    @Test
    void getByIds_withPartiallyExistingIds_returnsOnlyExistingBlogs() {
        // given
        TechBlogIdentity identity1 = new TechBlogIdentity(1L);
        TechBlogIdentity identity2 = new TechBlogIdentity(999L); // non-existing
        List<TechBlogIdentity> identities = List.of(identity1, identity2);

        List<TechBlog> blogs = List.of(sampleBlog); // only one exists
        when(techBlogRepository.findByIdsIn(identities))
                .thenReturn(blogs);

        // when
        List<TechBlog> result = techBlogReader.getByIds(identities);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleBlog.getTechBlogId(), result.get(0).getTechBlogId());
        verify(techBlogRepository).findByIdsIn(identities);
    }

    @Test
    void getAll_withData_returnsList() {
        // given
        List<TechBlog> blogs = List.of(sampleBlog);
        when(techBlogRepository.findAll())
                .thenReturn(blogs);

        // when
        List<TechBlog> result = techBlogReader.getAll();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleBlog.getTechBlogId(), result.get(0).getTechBlogId());
        verify(techBlogRepository).findAll();
    }

    @Test
    void getAll_emptyData_returnsEmptyList() {
        // given
        when(techBlogRepository.findAll())
                .thenReturn(List.of());

        // when
        List<TechBlog> result = techBlogReader.getAll();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(techBlogRepository).findAll();
    }

    @Test
    void findByUrl_existingUrl_returnsOptionalWithBlog() {
        // given
        String url = "https://example.com/blog/1";
        when(techBlogRepository.findByUrl(url))
                .thenReturn(Optional.of(sampleBlog));

        // when
        Optional<TechBlog> result = techBlogReader.findByUrl(url);

        // then
        assertTrue(result.isPresent());
        assertEquals(url, result.get().getUrl());
        verify(techBlogRepository).findByUrl(url);
    }

    @Test
    void findByUrl_nonExistingUrl_returnsEmptyOptional() {
        // given
        String url = "https://example.com/nonexistent";
        when(techBlogRepository.findByUrl(url))
                .thenReturn(Optional.empty());

        // when
        Optional<TechBlog> result = techBlogReader.findByUrl(url);

        // then
        assertFalse(result.isPresent());
        verify(techBlogRepository).findByUrl(url);
    }

    @Test
    void getByCompany_existingCompany_returnsList() {
        // given
        String company = "test company";
        List<TechBlog> blogs = List.of(sampleBlog);
        when(techBlogRepository.findByCompany(company))
                .thenReturn(blogs);

        // when
        List<TechBlog> result = techBlogReader.getByCompany(company);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(company, result.get(0).getCompany());
        verify(techBlogRepository).findByCompany(company);
    }

    @Test
    void getByCompany_nonExistingCompany_returnsEmptyList() {
        // given
        String company = "nonexistent company";
        when(techBlogRepository.findByCompany(company))
                .thenReturn(List.of());

        // when
        List<TechBlog> result = techBlogReader.getByCompany(company);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(techBlogRepository).findByCompany(company);
    }
}

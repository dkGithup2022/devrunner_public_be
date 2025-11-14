package dev.devrunner.service.communitypost.impl;

import dev.devrunner.exception.communitypost.CommunityPostNotFoundException;
import dev.devrunner.infra.communitypost.repository.CommunityPostRepository;
import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostCategory;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.communitypost.CommunityPostRead;
import dev.devrunner.model.communitypost.LinkedContent;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.service.communitypost.view.CommunityPostViewMemory;
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
class DefaultCommunityPostReaderTest {

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private CommunityPostViewMemory communityPostViewMemory;

    @InjectMocks
    private DefaultCommunityPostReader communityPostReader;

    // CommunityPostRead for read operations (with nickname)
    private static final CommunityPostRead samplePostRead = new CommunityPostRead(
        1L,                                  // communityPostId
        1L,                                  // userId
        "testuser",                          // nickname
        CommunityPostCategory.INTERVIEW_SHARE, // category
        "test title",                        // title
        "test markdown body",                // markdownBody
        "test company",                      // company
        "test location",                     // location
        LinkedContent.none(),                // linkedContent
        Popularity.empty(),                  // popularity
        false,                               // isDeleted
        Instant.now(),                       // createdAt
        Instant.now()                        // updatedAt
    );

    private final CommunityPostIdentity testIdentity = new CommunityPostIdentity(1L);

    @Test
    void read_existingId_returnsPostAndIncrementsViewCount() {
        // given
        when(communityPostRepository.findById(testIdentity))
            .thenReturn(Optional.of(samplePostRead));

        // when
        CommunityPostRead result = communityPostReader.read(testIdentity);

        // then
        assertNotNull(result);
        assertEquals(samplePostRead.getCommunityPostId(), result.getCommunityPostId());
        assertEquals(samplePostRead.getTitle(), result.getTitle());
        assertEquals(samplePostRead.getNickname(), result.getNickname());
        verify(communityPostRepository).findById(testIdentity);
        verify(communityPostViewMemory).countUp(1L);
    }

    @Test
    void read_nonExistingId_throwsCommunityPostNotFoundException() {
        // given
        when(communityPostRepository.findById(testIdentity))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(CommunityPostNotFoundException.class, () ->
            communityPostReader.read(testIdentity)
        );
        verify(communityPostRepository).findById(testIdentity);
        verify(communityPostViewMemory, never()).countUp(any());
    }

    @Test
    void getById_existingId_returnsPostWithoutViewCount() {
        // given
        when(communityPostRepository.findById(testIdentity))
            .thenReturn(Optional.of(samplePostRead));

        // when
        CommunityPostRead result = communityPostReader.getById(testIdentity);

        // then
        assertNotNull(result);
        assertEquals(samplePostRead.getCommunityPostId(), result.getCommunityPostId());
        assertEquals(samplePostRead.getTitle(), result.getTitle());
        verify(communityPostRepository).findById(testIdentity);
        verify(communityPostViewMemory, never()).countUp(any());
    }

    @Test
    void getById_nonExistingId_throwsCommunityPostNotFoundException() {
        // given
        when(communityPostRepository.findById(testIdentity))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(CommunityPostNotFoundException.class, () ->
            communityPostReader.getById(testIdentity)
        );
        verify(communityPostRepository).findById(testIdentity);
    }

    @Test
    void getAll_withData_returnsList() {
        // given
        List<CommunityPostRead> posts = List.of(samplePostRead);
        when(communityPostRepository.findAll())
            .thenReturn(posts);

        // when
        List<CommunityPostRead> result = communityPostReader.getAll();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(samplePostRead.getCommunityPostId(), result.get(0).getCommunityPostId());
        verify(communityPostRepository).findAll();
    }

    @Test
    void getAll_emptyData_returnsEmptyList() {
        // given
        when(communityPostRepository.findAll())
            .thenReturn(List.of());

        // when
        List<CommunityPostRead> result = communityPostReader.getAll();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(communityPostRepository).findAll();
    }

    @Test
    void getByUserId_existingUser_returnsList() {
        // given
        Long userId = 1L;
        List<CommunityPostRead> posts = List.of(samplePostRead);
        when(communityPostRepository.findByUserId(userId))
            .thenReturn(posts);

        // when
        List<CommunityPostRead> result = communityPostReader.getByUserId(userId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(communityPostRepository).findByUserId(userId);
    }

    @Test
    void getByUserId_nonExistingUser_returnsEmptyList() {
        // given
        Long userId = 999L;
        when(communityPostRepository.findByUserId(userId))
            .thenReturn(List.of());

        // when
        List<CommunityPostRead> result = communityPostReader.getByUserId(userId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(communityPostRepository).findByUserId(userId);
    }

    @Test
    void getByCompany_existingCompany_returnsList() {
        // given
        String company = "test company";
        List<CommunityPostRead> posts = List.of(samplePostRead);
        when(communityPostRepository.findByCompany(company))
            .thenReturn(posts);

        // when
        List<CommunityPostRead> result = communityPostReader.getByCompany(company);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(company, result.get(0).getCompany());
        verify(communityPostRepository).findByCompany(company);
    }

    @Test
    void getByCompany_nonExistingCompany_returnsEmptyList() {
        // given
        String company = "nonexistent company";
        when(communityPostRepository.findByCompany(company))
            .thenReturn(List.of());

        // when
        List<CommunityPostRead> result = communityPostReader.getByCompany(company);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(communityPostRepository).findByCompany(company);
    }

    @Test
    void getByLocation_existingLocation_returnsList() {
        // given
        String location = "test location";
        List<CommunityPostRead> posts = List.of(samplePostRead);
        when(communityPostRepository.findByLocation(location))
            .thenReturn(posts);

        // when
        List<CommunityPostRead> result = communityPostReader.getByLocation(location);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(location, result.get(0).getLocation());
        verify(communityPostRepository).findByLocation(location);
    }

    @Test
    void getByLocation_nonExistingLocation_returnsEmptyList() {
        // given
        String location = "nonexistent location";
        when(communityPostRepository.findByLocation(location))
            .thenReturn(List.of());

        // when
        List<CommunityPostRead> result = communityPostReader.getByLocation(location);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(communityPostRepository).findByLocation(location);
    }

    @Test
    void getByIds_withExistingIds_returnsList() {
        // given
        CommunityPostIdentity identity1 = new CommunityPostIdentity(1L);
        CommunityPostIdentity identity2 = new CommunityPostIdentity(2L);
        List<CommunityPostIdentity> identities = List.of(identity1, identity2);

        CommunityPostRead post2 = new CommunityPostRead(
            2L, 1L, "testuser", CommunityPostCategory.INTERVIEW_SHARE,
            "test title 2", "test markdown body 2", "test company 2", "test location 2",
            LinkedContent.none(), Popularity.empty(), false, Instant.now(), Instant.now()
        );

        List<CommunityPostRead> posts = List.of(samplePostRead, post2);
        when(communityPostRepository.findByIds(identities))
            .thenReturn(posts);

        // when
        List<CommunityPostRead> result = communityPostReader.getByIds(identities);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(communityPostRepository).findByIds(identities);
    }

    @Test
    void getByIds_withEmptyIds_returnsEmptyList() {
        // given
        List<CommunityPostIdentity> emptyIdentities = List.of();
        when(communityPostRepository.findByIds(emptyIdentities))
            .thenReturn(List.of());

        // when
        List<CommunityPostRead> result = communityPostReader.getByIds(emptyIdentities);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(communityPostRepository).findByIds(emptyIdentities);
    }

    @Test
    void getByIds_withPartiallyExistingIds_returnsOnlyExistingPosts() {
        // given
        CommunityPostIdentity identity1 = new CommunityPostIdentity(1L);
        CommunityPostIdentity identity2 = new CommunityPostIdentity(999L); // non-existing
        List<CommunityPostIdentity> identities = List.of(identity1, identity2);

        List<CommunityPostRead> posts = List.of(samplePostRead); // only one exists
        when(communityPostRepository.findByIds(identities))
            .thenReturn(posts);

        // when
        List<CommunityPostRead> result = communityPostReader.getByIds(identities);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(samplePostRead.getCommunityPostId(), result.get(0).getCommunityPostId());
        verify(communityPostRepository).findByIds(identities);
    }
}

package dev.devrunner.api.communitypost;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.api.communitypost.dto.CommunityPostRequest;
import dev.devrunner.api.communitypost.dto.CommunityPostResponse;
import dev.devrunner.auth.model.SessionUser;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostCategory;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.communitypost.CommunityPostRead;
import dev.devrunner.model.communitypost.LinkedContent;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.communitypost.CommunityPostReader;
import dev.devrunner.service.communitypost.CommunityPostWriter;
import dev.devrunner.service.communitypost.dto.CommunityPostUpsertCommand;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityPostApiControllerTest {

    @Mock
    private CommunityPostReader communityPostReader;

    @Mock
    private CommunityPostWriter communityPostWriter;

    @InjectMocks
    private CommunityPostApiController communityPostApiController;

    private ObjectMapper objectMapper;

    private final SessionUser sampleSessionUser = SessionUser.of(1L, Instant.now(), Instant.now().plusSeconds(1000*60));

    private final CommunityPost sampleCommunityPost = new CommunityPost(
            1L,                                  // communityPostId
            1L,                                  // userId
            CommunityPostCategory.INTERVIEW_SHARE, // category
            "Test Community Post Title",         // title
            "Test markdown body content",        // markdownBody
            "Test Company",                      // company
            "Seoul",                            // location
            LinkedContent.fromJob(100L),        // linkedContent
            Popularity.empty(),                 // popularity
            false,                              // isDeleted
            Instant.now(),                      // createdAt
            Instant.now()                       // updatedAt
    );

    private final CommunityPostRead sampleCommunityPostRead = new CommunityPostRead(
            1L,                                  // communityPostId
            1L,                                  // userId
            "testuser",                          // nickname
            CommunityPostCategory.INTERVIEW_SHARE, // category
            "Test Community Post Title",         // title
            "Test markdown body content",        // markdownBody
            "Test Company",                      // company
            "Seoul",                            // location
            LinkedContent.fromJob(100L),        // linkedContent
            Popularity.empty(),                 // popularity
            false,                              // isDeleted
            Instant.now(),                      // createdAt
            Instant.now()                       // updatedAt
    );

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // JavaTimeModule 등록
    }

    @Test
    void getCommunityPost_asOwner_returnsOkWithRealNickname() {
        // given
        when(communityPostReader.read(new CommunityPostIdentity(1L)))
                .thenReturn(sampleCommunityPostRead);

        // when - 소유자(userId=1)가 조회
        ResponseEntity<CommunityPostResponse> response =
                communityPostApiController.getCommunityPost(sampleSessionUser, 1L);

        // then - INTERVIEW_SHARE지만 소유자이므로 실제 nickname
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCommunityPostId()).isEqualTo(1L);
        assertThat(response.getBody().getUserId()).isEqualTo(1L);
        assertThat(response.getBody().getNickname()).isEqualTo("testuser");
        assertThat(response.getBody().getCategory()).isEqualTo(CommunityPostCategory.INTERVIEW_SHARE);
        assertThat(response.getBody().getTitle()).isEqualTo("Test Community Post Title");

        verify(communityPostReader).read(new CommunityPostIdentity(1L));
    }

    @Test
    void getCommunityPost_asNonOwner_returnsOkWithAnonymous() {
        // given
        when(communityPostReader.read(new CommunityPostIdentity(1L)))
                .thenReturn(sampleCommunityPostRead);
        SessionUser otherUser = SessionUser.of(999L, Instant.now(), Instant.now().plusSeconds(1000*60));

        // when - 비소유자(userId=999)가 INTERVIEW_SHARE 조회
        ResponseEntity<CommunityPostResponse> response =
                communityPostApiController.getCommunityPost(otherUser, 1L);

        // then - INTERVIEW_SHARE이고 비소유자이므로 Anonymous
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNickname()).isEqualTo("Anonymous");
        assertThat(response.getBody().getCategory()).isEqualTo(CommunityPostCategory.INTERVIEW_SHARE);

        verify(communityPostReader).read(new CommunityPostIdentity(1L));
    }

    @Test
    void getCommunityPost_asGuest_returnsOkWithAnonymous() {
        // given
        when(communityPostReader.read(new CommunityPostIdentity(1L)))
                .thenReturn(sampleCommunityPostRead);

        // when - 비로그인(null) 사용자가 INTERVIEW_SHARE 조회
        ResponseEntity<CommunityPostResponse> response =
                communityPostApiController.getCommunityPost(null, 1L);

        // then - INTERVIEW_SHARE이고 비로그인이므로 Anonymous
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNickname()).isEqualTo("Anonymous");
        assertThat(response.getBody().getCategory()).isEqualTo(CommunityPostCategory.INTERVIEW_SHARE);

        verify(communityPostReader).read(new CommunityPostIdentity(1L));
    }

    @Test
    void getCommunityPost_nonExistingId_throwsException() {
        // given
        when(communityPostReader.read(new CommunityPostIdentity(999L)))
                .thenThrow(new RuntimeException("Community post not found"));

        // when & then
        try {
            communityPostApiController.getCommunityPost(sampleSessionUser, 999L);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Community post not found");
        }

        verify(communityPostReader).read(new CommunityPostIdentity(999L));
    }

    @Test
    void upsertCommunityPost_newPost_returnsCreatedWithPost() {
        // given
        CommunityPostRequest request = new CommunityPostRequest(
                null,                            // communityPostId
                CommunityPostCategory.INTERVIEW_SHARE,
                "Test Community Post Title",
                "Test markdown body content",
                "Test Company",
                "Seoul",
                100L,                            // jobId
                null                             // commentId
        );

        when(communityPostWriter.upsert(any(CommunityPostUpsertCommand.class)))
                .thenReturn(sampleCommunityPost);
        when(communityPostReader.getById(new CommunityPostIdentity(1L)))
                .thenReturn(sampleCommunityPostRead);

        // when
        ResponseEntity<CommunityPostResponse> response =
                communityPostApiController.upsertCommunityPost(sampleSessionUser, request);

        // then - 소유자가 자신의 포스트를 생성하므로 실제 nickname
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCommunityPostId()).isEqualTo(1L);
        assertThat(response.getBody().getTitle()).isEqualTo("Test Community Post Title");
        assertThat(response.getBody().getNickname()).isEqualTo("testuser");

        verify(communityPostWriter).upsert(any(CommunityPostUpsertCommand.class));
        verify(communityPostReader).getById(new CommunityPostIdentity(1L));
    }

    @Test
    void upsertCommunityPost_existingPost_returnsCreatedWithUpdatedPost() {
        // given
        CommunityPostRequest request = new CommunityPostRequest(
                1L,                              // communityPostId (existing)
                CommunityPostCategory.INTERVIEW_SHARE,
                "Updated Title",
                "Updated body",
                "Test Company",
                "Seoul",
                100L,
                null
        );

        CommunityPost updatedPost = new CommunityPost(
                1L, 1L, CommunityPostCategory.INTERVIEW_SHARE,
                "Updated Title", "Updated body",
                "Test Company", "Seoul", LinkedContent.fromJob(100L),
                Popularity.empty(), false,
                Instant.now(), Instant.now()
        );
        CommunityPostRead updatedPostRead = new CommunityPostRead(
                1L, 1L, "testuser", CommunityPostCategory.INTERVIEW_SHARE,
                "Updated Title", "Updated body",
                "Test Company", "Seoul", LinkedContent.fromJob(100L),
                Popularity.empty(), false,
                Instant.now(), Instant.now()
        );

        when(communityPostWriter.upsert(any(CommunityPostUpsertCommand.class)))
                .thenReturn(updatedPost);
        when(communityPostReader.getById(new CommunityPostIdentity(1L)))
                .thenReturn(updatedPostRead);

        // when
        ResponseEntity<CommunityPostResponse> response =
                communityPostApiController.upsertCommunityPost(sampleSessionUser, request);

        // then - 소유자가 자신의 포스트를 수정하므로 실제 nickname
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCommunityPostId()).isEqualTo(1L);
        assertThat(response.getBody().getTitle()).isEqualTo("Updated Title");
        assertThat(response.getBody().getNickname()).isEqualTo("testuser");

        verify(communityPostWriter).upsert(any(CommunityPostUpsertCommand.class));
        verify(communityPostReader).getById(new CommunityPostIdentity(1L));
    }

    @Test
    void deleteCommunityPost_existingId_returnsNoContent() {
        // given
        doNothing().when(communityPostWriter).delete(new UserIdentity(1L), new CommunityPostIdentity(1L));

        // when
        ResponseEntity<Void> response =
                communityPostApiController.deleteCommunityPost(sampleSessionUser, 1L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(communityPostWriter).delete(new UserIdentity(1L), new CommunityPostIdentity(1L));
    }

    @Test
    void deleteCommunityPost_nonExistingId_throwsException() {
        // given
        doThrow(new RuntimeException("Community post not found"))
                .when(communityPostWriter).delete(new UserIdentity(1L), new CommunityPostIdentity(999L));

        // when & then
        try {
            communityPostApiController.deleteCommunityPost(sampleSessionUser, 999L);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Community post not found");
        }

        verify(communityPostWriter).delete(new UserIdentity(1L), new CommunityPostIdentity(999L));
    }
}

package dev.devrunner.service.techblog.impl;

import dev.devrunner.infra.techblog.repository.TechBlogRepository;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.model.techblog.TechBlogIdentity;
import dev.devrunner.outbox.command.RecordOutboxEventCommand;
import dev.devrunner.outbox.model.UpdateType;
import dev.devrunner.outbox.recorder.OutboxEventRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultTechBlogWriterTest {

    @Mock
    private TechBlogRepository techBlogRepository;

    @InjectMocks
    private DefaultTechBlogWriter techBlogWriter;

    @Mock
    private OutboxEventRecorder outboxEventRecorder;

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

    @Test
    void upsert_newBlog_callsRepositorySave() {
        // given
        TechBlog newBlog = TechBlog.newBlog(
                "https://example.com/blog/new",
                "new title",
                "new markdown body"
        );
        when(techBlogRepository.save(any(TechBlog.class)))
                .thenReturn(sampleBlog);
        when(outboxEventRecorder.record(any())).thenReturn(null);

        // when
        TechBlog result = techBlogWriter.upsert(newBlog);

        // then
        assertNotNull(result);
        assertEquals(sampleBlog.getTechBlogId(), result.getTechBlogId());
        verify(techBlogRepository).save(newBlog);
    }

    @Test
    void upsert_existingBlog_callsRepositorySave() {
        // given
        TechBlog updatedBlog = new TechBlog(
                1L, "https://example.com/blog/1", "updated company", "updated title",
                null, null, null, "updated body", "https://example.com/updated.jpg",
                List.of(),
                "https://original.com/updated", Popularity.empty(), false,
                Instant.now(), Instant.now()
        );
        when(techBlogRepository.save(any(TechBlog.class)))
                .thenReturn(updatedBlog);
        when(outboxEventRecorder.record(any())).thenReturn(null);
        // when
        TechBlog result = techBlogWriter.upsert(updatedBlog);

        // then
        assertNotNull(result);
        assertEquals("updated company", result.getCompany());
        assertEquals("updated title", result.getTitle());
        verify(techBlogRepository).save(updatedBlog);
    }

    @Test
    void upsert_externalBlog_callsRepositorySave() {
        // given
        TechBlog externalBlog = TechBlog.newExternalBlog(
                "https://example.com/external",
                "external company",
                "external title",
                "external body",
                "https://original-external.com/post"
        );
        when(techBlogRepository.save(any(TechBlog.class)))
                .thenReturn(externalBlog);
        when(outboxEventRecorder.record(any())).thenReturn(null);
        // when
        TechBlog result = techBlogWriter.upsert(externalBlog);

        // then
        assertNotNull(result);
        assertEquals("external company", result.getCompany());
        assertEquals("https://original-external.com/post", result.getOriginalUrl());
        verify(techBlogRepository).save(externalBlog);
    }

    @Test
    void upsert_blogWithCategories_callsRepositorySave() {
        // given
        TechBlog blogWithMeta = new TechBlog(
                null, "https://example.com/meta", "meta company", "meta title",
                null, null, null, "meta body", null, List.of(),
                null, Popularity.empty(), false, Instant.now(), Instant.now()
        );
        when(techBlogRepository.save(any(TechBlog.class)))
                .thenReturn(blogWithMeta);
        when(outboxEventRecorder.record(any())).thenReturn(null);
        // when
        TechBlog result = techBlogWriter.upsert(blogWithMeta);

        // then
        assertNotNull(result);
        assertEquals(0, result.getTechCategories().size());
        verify(techBlogRepository).save(blogWithMeta);
    }

    @Test
    void delete_existingBlog_callsRepositoryDelete() {
        // given
        TechBlogIdentity identity = new TechBlogIdentity(1L);
        doNothing().when(techBlogRepository).deleteById(identity);

        // when
        techBlogWriter.delete(identity);

        // then
        verify(techBlogRepository).deleteById(identity);
    }

    @Test
    void delete_callsRepositoryDeleteWithCorrectIdentity() {
        // given
        TechBlogIdentity identity = new TechBlogIdentity(999L);
        doNothing().when(techBlogRepository).deleteById(identity);

        // when
        techBlogWriter.delete(identity);

        // then
        verify(techBlogRepository).deleteById(identity);
    }

    @Test
    void upsert_newBlog_recordsCreatedEvent() {
        // given
        TechBlog newBlog = TechBlog.newBlog(
                "https://example.com/blog/new",
                "new title",
                "new markdown body"
        );
        TechBlog savedBlog = new TechBlog(
                100L, "https://example.com/blog/new", "test company", "new title",
                null, null, null, "new markdown body", null, List.of(),
                null, Popularity.empty(), false, Instant.now(), Instant.now()
        );
        when(techBlogRepository.save(any(TechBlog.class)))
                .thenReturn(savedBlog);

        ArgumentCaptor<RecordOutboxEventCommand> eventCaptor = ArgumentCaptor.forClass(RecordOutboxEventCommand.class);

        // when
        techBlogWriter.upsert(newBlog);

        // then
        verify(outboxEventRecorder).record(eventCaptor.capture());
        RecordOutboxEventCommand capturedEvent = eventCaptor.getValue();
        assertEquals(TargetType.TECH_BLOG, capturedEvent.getTargetType());
        assertEquals(100L, capturedEvent.getTargetId());
        assertEquals(UpdateType.CREATED, capturedEvent.getUpdateType());
    }

    @Test
    void upsert_existingBlog_recordsUpdatedEvent() {
        // given
        TechBlog existingBlog = new TechBlog(
                1L, "https://example.com/blog/1", "updated company", "updated title",
                null, null, null, "updated body", "https://example.com/updated.jpg",
                List.of(), "https://original.com/updated", Popularity.empty(), false,
                Instant.now(), Instant.now()
        );
        when(techBlogRepository.save(any(TechBlog.class)))
                .thenReturn(existingBlog);

        ArgumentCaptor<RecordOutboxEventCommand> eventCaptor = ArgumentCaptor.forClass(RecordOutboxEventCommand.class);

        // when
        techBlogWriter.upsert(existingBlog);

        // then
        verify(outboxEventRecorder).record(eventCaptor.capture());
        RecordOutboxEventCommand capturedEvent = eventCaptor.getValue();
        assertEquals(TargetType.TECH_BLOG, capturedEvent.getTargetType());
        assertEquals(1L, capturedEvent.getTargetId());
        assertEquals(UpdateType.UPDATED, capturedEvent.getUpdateType());
    }
}

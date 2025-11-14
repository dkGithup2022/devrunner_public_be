package dev.devrunner.service.communitypost.impl;

import dev.devrunner.exception.auth.UnauthorizedException;
import dev.devrunner.exception.communitypost.CommunityPostNotFoundException;
import dev.devrunner.infra.user.repository.UserRepository;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.communitypost.CommunityPostRead;
import dev.devrunner.model.communitypost.LinkedContent;
import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.outbox.command.RecordOutboxEventCommand;
import dev.devrunner.outbox.recorder.OutboxEventRecorder;
import dev.devrunner.service.communitypost.CommunityPostWriter;
import dev.devrunner.infra.communitypost.repository.CommunityPostRepository;
import dev.devrunner.service.communitypost.dto.CommunityPostUpsertCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * CommunityPost 도메인 변경 서비스 구현체
 * <p>
 * CQRS 패턴의 Command 책임을 구현하며,
 * Infrastructure Repository를 활용한 변경 로직을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultCommunityPostWriter implements CommunityPostWriter {

    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    private final OutboxEventRecorder outboxEventRecorder;

    @Override
    @Transactional
    public CommunityPost upsert(CommunityPostUpsertCommand command) {
        log.info("Upserting CommunityPost - userId: {}, communityPostId: {}",
                command.getRequestUserId(), command.getCommunityPostId());

        boolean isNewPost = (command.getCommunityPostId() == null);

        // Command → Domain 변환
        CommunityPost communityPost = toCommunityPost(command);
        CommunityPost saved = communityPostRepository.save(communityPost);

        // 새 게시글이면 작성자의 postCount 증가
        if (isNewPost) {
            UserIdentity authorIdentity = new UserIdentity(command.getRequestUserId());
            User author = userRepository.findById(authorIdentity)
                    .orElseThrow(() -> new RuntimeException("User not found: " + command.getRequestUserId()));

            User updatedAuthor = author.incrementPostCount();
            userRepository.save(updatedAuthor);
            log.info("User {} postCount incremented", command.getRequestUserId());
        }

        RecordOutboxEventCommand recordCommand = isNewPost
                ? RecordOutboxEventCommand.created(TargetType.COMMUNITY_POST, saved.getCommunityPostId())
                : RecordOutboxEventCommand.updated(TargetType.COMMUNITY_POST, saved.getCommunityPostId());
        outboxEventRecorder.record(recordCommand);

        log.info("CommunityPost upserted successfully: {}", saved.getCommunityPostId());
        return saved;
    }

    private CommunityPost toCommunityPost(CommunityPostUpsertCommand command) {
        Instant now = Instant.now();
        LinkedContent linkedContent = createLinkedContent(command.getJobId(), command.getCommentId());

        return new CommunityPost(
                command.getCommunityPostId(),
                command.getRequestUserId(),
                command.getCategory(),
                command.getTitle(),
                command.getMarkdownBody(),
                command.getCompany(),
                command.getLocation(),
                linkedContent,
                Popularity.empty(),
                false,
                now,
                now
        );
    }

    private LinkedContent createLinkedContent(Long jobId, Long commentId) {
        if (jobId == null && commentId == null) {
            return LinkedContent.none();
        }
        if (commentId != null) {
            return LinkedContent.fromJobComment(jobId, commentId);
        }
        return LinkedContent.fromJob(jobId);
    }

    @Override
    public void delete(UserIdentity requestUser, CommunityPostIdentity identity) {
        log.info("Deleting CommunityPost by id: {} / requestUserId: {}",
                identity.getCommunityPostId(), requestUser.getUserId());

        // 1. 게시글 조회
        CommunityPostRead communityPostRead = communityPostRepository.findById(identity)
                .orElseThrow(() -> new CommunityPostNotFoundException("communityPostId=" + identity.getCommunityPostId() + " not found"));

        // 2. Ownership 검증
        if (!communityPostRead.getUserId().equals(requestUser.getUserId()))
            throw new UnauthorizedException("You are not authorized to delete this community post");

        // 3. 삭제
        communityPostRepository.deleteById(identity);
        log.info("CommunityPost deleted successfully: {}", identity.getCommunityPostId());


        RecordOutboxEventCommand recordCommand = RecordOutboxEventCommand.updated(TargetType.COMMUNITY_POST, identity.getCommunityPostId());
        outboxEventRecorder.record(recordCommand);
    }

    /**
     * CommunityPostRead → CommunityPost 변환 (nickname 제외)
     */
    private CommunityPost toCommunityPost(CommunityPostRead communityPostRead) {
        return new CommunityPost(
                communityPostRead.getCommunityPostId(),
                communityPostRead.getUserId(),
                communityPostRead.getCategory(),
                communityPostRead.getTitle(),
                communityPostRead.getMarkdownBody(),
                communityPostRead.getCompany(),
                communityPostRead.getLocation(),
                communityPostRead.getLinkedContent(),
                communityPostRead.getPopularity(),
                communityPostRead.getIsDeleted(),
                communityPostRead.getCreatedAt(),
                communityPostRead.getUpdatedAt()
        );
    }
}

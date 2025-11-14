package dev.devrunner.service.reaction.impl;

import dev.devrunner.exception.reaction.ReactionConflictException;
import dev.devrunner.infra.communitypost.repository.CommunityPostRepository;
import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.infra.reaction.repository.ReactionRepository;
import dev.devrunner.infra.user.repository.UserRepository;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.communitypost.CommunityPostRead;
import dev.devrunner.model.job.JobIdentity;
import dev.devrunner.model.reaction.Reaction;
import dev.devrunner.model.reaction.ReactionIdentity;
import dev.devrunner.model.reaction.ReactionType;
import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.reaction.ReactionReader;
import dev.devrunner.service.reaction.ReactionWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Reaction 서비스 구현체
 *
 * 좋아요/싫어요 반응을 처리하는 비즈니스 로직을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultReactionWriter implements ReactionWriter {

    private final ReactionReader reactionReader;
    private final ReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final CommunityPostRepository communityPostRepository;

    @Override
    @Transactional
    public void likeUp(UserIdentity user, TargetType targetType, Long targetId) {
        log.info("User {} adding LIKE to {} {}", user.getUserId(), targetType, targetId);

        // 트랜잭션 시작 직후 아티클의 like_count에 +0 (row lock 획득)
        // → 중복 체크가 직렬화되어 동시성 문제 방지
        incrementArticleLikeCount(targetType, targetId, 0);

        Optional<Reaction> existing = reactionReader.findByUserIdAndTargetTypeAndTargetId(user.getUserId(), targetType, targetId);

        if (existing.isPresent()) {
           throw new ReactionConflictException("User already liked to " + targetType);
        } else {
            // 새로운 LIKE 생성
            Reaction newReaction = Reaction.create(user.getUserId(), targetType, targetId, ReactionType.LIKE);
            reactionRepository.save(newReaction);
            log.info("User {} created new LIKE on {} {}", user.getUserId(), targetType, targetId);

            // 새로운 LIKE 생성 시: like_count +1
            incrementArticleLikeCount(targetType, targetId, 1);

            // 새로운 LIKE 생성 시 likeGivenCount 증가 및 대상 작성자의 likesReceived 증가
            incrementUserLikeGivenCount(user);
            incrementTargetAuthorLikesReceived(targetType, targetId);
        }
    }

    @Override
    @Transactional
    public void dislikeUp(UserIdentity user, TargetType targetType, Long targetId) {
        log.info("User {} adding DISLIKE to {} {}", user.getUserId(), targetType, targetId);

        // 트랜잭션 시작 직후 아티클의 dislike_count에 +0 (row lock 획득)
        incrementArticleDislikeCount(targetType, targetId, 0);

        Optional<Reaction> existing = reactionReader.findByUserIdAndTargetTypeAndTargetId(user.getUserId(), targetType, targetId);

        if (existing.isPresent()) {
            throw new ReactionConflictException("User already liked to " + targetType);
        } else {
            // 새로운 DISLIKE 생성
            Reaction newReaction = Reaction.create(user.getUserId(), targetType, targetId, ReactionType.DISLIKE);
            reactionRepository.save(newReaction);
            log.info("User {} created new DISLIKE on {} {}", user.getUserId(), targetType, targetId);

            // 새로운 DISLIKE 생성 시: dislike_count +1
            incrementArticleDislikeCount(targetType, targetId, 1);
        }
    }

    @Override
    @Transactional
    public void likeDown(UserIdentity user, TargetType targetType, Long targetId) {
        log.info("User {} removing LIKE from {} {}", user.getUserId(), targetType, targetId);

        Optional<Reaction> existing = reactionReader.findByUserIdAndTargetTypeAndTargetId(user.getUserId(), targetType, targetId);

        if (existing.isEmpty()) {
            log.info("No reaction found for user {} on {} {}", user.getUserId(), targetType, targetId);
            return;
        }

        Reaction reaction = existing.get();
        if (reaction.getReactionType() != ReactionType.LIKE) {
            log.warn("User {} tried to remove LIKE but has DISLIKE on {} {}", user.getUserId(), targetType, targetId);
            return;
        }

        reactionRepository.deleteById(new ReactionIdentity(reaction.getReactionId()));
        log.info("User {} removed LIKE from {} {}", user.getUserId(), targetType, targetId);

        // LIKE 삭제 시: like_count -1
        incrementArticleLikeCount(targetType, targetId, -1);

        // LIKE 삭제 시 likeGivenCount 감소 및 대상 작성자의 likesReceived 감소
        decrementUserLikeGivenCount(user);
        decrementTargetAuthorLikesReceived(targetType, targetId);
    }

    @Override
    @Transactional
    public void dislikeDown(UserIdentity user, TargetType targetType, Long targetId) {
        log.info("User {} removing DISLIKE from {} {}", user.getUserId(), targetType, targetId);

        Optional<Reaction> existing = reactionReader.findByUserIdAndTargetTypeAndTargetId(user.getUserId(), targetType, targetId);

        if (existing.isEmpty()) {
            log.info("No reaction found for user {} on {} {}", user.getUserId(), targetType, targetId);
            return;
        }

        Reaction reaction = existing.get();
        if (reaction.getReactionType() != ReactionType.DISLIKE) {
            log.warn("User {} tried to remove DISLIKE but has LIKE on {} {}", user.getUserId(), targetType, targetId);
            return;
        }

        reactionRepository.deleteById(new ReactionIdentity(reaction.getReactionId()));
        log.info("User {} removed DISLIKE from {} {}", user.getUserId(), targetType, targetId);

        // DISLIKE 삭제 시: dislike_count -1
        incrementArticleDislikeCount(targetType, targetId, -1);
    }

    /**
     * User의 likeGivenCount 증가
     */
    private void incrementUserLikeGivenCount(UserIdentity userIdentity) {
        User user = userRepository.findById(userIdentity)
                .orElseThrow(() -> new RuntimeException("User not found: " + userIdentity.getUserId()));

        User updatedUser = user.incrementLikeGivenCount();
        userRepository.save(updatedUser);
        log.info("User {} likeGivenCount incremented", userIdentity.getUserId());
    }

    /**
     * User의 likeGivenCount 감소
     */
    private void decrementUserLikeGivenCount(UserIdentity userIdentity) {
        User user = userRepository.findById(userIdentity)
                .orElseThrow(() -> new RuntimeException("User not found: " + userIdentity.getUserId()));

        User updatedUser = user.decrementLikeGivenCount();
        userRepository.save(updatedUser);
        log.info("User {} likeGivenCount decremented", userIdentity.getUserId());
    }

    /**
     * 대상의 작성자 likesReceived 증가
     * COMMUNITY_POST만 작성자가 있음 (JOB, TECH_BLOG는 크롤링 데이터)
     */
    private void incrementTargetAuthorLikesReceived(TargetType targetType, Long targetId) {
        if (targetType != TargetType.COMMUNITY_POST) {
            // JOB, TECH_BLOG는 작성자가 없으므로 카운트 업데이트 불필요
            return;
        }

        Optional<CommunityPostRead> communityPostOpt = communityPostRepository.findById(new CommunityPostIdentity(targetId));
        if (communityPostOpt.isEmpty()) {
            log.warn("CommunityPost {} not found, skipping author likesReceived increment", targetId);
            return;
        }

        CommunityPostRead communityPost = communityPostOpt.get();
        UserIdentity authorIdentity = new UserIdentity(communityPost.getUserId());

        Optional<User> authorOpt = userRepository.findById(authorIdentity);
        if (authorOpt.isEmpty()) {
            log.warn("Author {} not found (may be deleted), skipping likesReceived increment", communityPost.getUserId());
            return;
        }

        User author = authorOpt.get();
        User updatedAuthor = author.incrementLikesReceived();
        userRepository.save(updatedAuthor);
        log.info("CommunityPost {} author {} likesReceived incremented", targetId, communityPost.getUserId());
    }

    /**
     * 대상의 작성자 likesReceived 감소
     * COMMUNITY_POST만 작성자가 있음 (JOB, TECH_BLOG는 크롤링 데이터)
     */
    private void decrementTargetAuthorLikesReceived(TargetType targetType, Long targetId) {
        if (targetType != TargetType.COMMUNITY_POST) {
            // JOB, TECH_BLOG는 작성자가 없으므로 카운트 업데이트 불필요
            return;
        }

        Optional<CommunityPostRead> communityPostOpt = communityPostRepository.findById(new CommunityPostIdentity(targetId));
        if (communityPostOpt.isEmpty()) {
            log.warn("CommunityPost {} not found, skipping author likesReceived decrement", targetId);
            return;
        }

        CommunityPostRead communityPost = communityPostOpt.get();
        UserIdentity authorIdentity = new UserIdentity(communityPost.getUserId());

        Optional<User> authorOpt = userRepository.findById(authorIdentity);
        if (authorOpt.isEmpty()) {
            log.warn("Author {} not found (may be deleted), skipping likesReceived decrement", communityPost.getUserId());
            return;
        }

        User author = authorOpt.get();
        User updatedAuthor = author.decrementLikesReceived();
        userRepository.save(updatedAuthor);
        log.info("CommunityPost {} author {} likesReceived decremented", targetId, communityPost.getUserId());
    }

    /**
     * 아티클(Job, CommunityPost)의 like_count 증가/감소
     */
    private void incrementArticleLikeCount(TargetType targetType, Long targetId, long increment) {
        switch (targetType) {
            case JOB:
                jobRepository.increaseLikeCount(new JobIdentity(targetId), increment);
                log.info("Job {} like_count changed by {}", targetId, increment);
                break;
            case COMMUNITY_POST:
                communityPostRepository.increaseLikeCount(new CommunityPostIdentity(targetId), increment);
                log.info("CommunityPost {} like_count changed by {}", targetId, increment);
                break;
            case TECH_BLOG:
                // TechBlog는 좋아요 기능 없음 - skip
                break;
            default:
                log.warn("Unknown target type: {}", targetType);
        }
    }

    /**
     * 아티클(Job, CommunityPost)의 dislike_count 증가/감소
     */
    private void incrementArticleDislikeCount(TargetType targetType, Long targetId, long increment) {
        switch (targetType) {
            case JOB:
                jobRepository.increaseDislikeCount(new JobIdentity(targetId), increment);
                log.info("Job {} dislike_count changed by {}", targetId, increment);
                break;
            case COMMUNITY_POST:
                communityPostRepository.increaseDislikeCount(new CommunityPostIdentity(targetId), increment);
                log.info("CommunityPost {} dislike_count changed by {}", targetId, increment);
                break;
            case TECH_BLOG:
                // TechBlog는 좋아요 기능 없음 - skip
                break;
            default:
                log.warn("Unknown target type: {}", targetType);
        }
    }
}

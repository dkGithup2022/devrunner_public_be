package dev.devrunner.jdbc.comment.repository;

import dev.devrunner.model.comment.Comment;
import dev.devrunner.model.comment.CommentIdentity;
import dev.devrunner.model.comment.CommentRead;
import dev.devrunner.model.common.CommentOrder;
import dev.devrunner.infra.comment.repository.CommentRepository;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Comment Repository 구현체
 * <p>
 * 헥사고날 아키텍처에서 Adapter 역할을 수행하며,
 * Infrastructure의 CommentRepository 인터페이스를
 * Spring Data JDBC를 활용하여 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class CommentJdbcRepository implements CommentRepository {

    private final CommentEntityRepository entityRepository;

    @Override
    public Optional<CommentRead> findById(CommentIdentity identity) {
        CommentWithUserDto dto = entityRepository.findByIdWithUser(identity.getCommentId());
        return Optional.ofNullable(dto).map(this::toCommentRead);
    }

    @Override
    public Comment save(Comment comment) {
        CommentEntity entity = toEntity(comment);
        CommentEntity saved = entityRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(CommentIdentity identity) {
        entityRepository.deleteById(identity.getCommentId());
    }

    @Override
    public boolean existsById(CommentIdentity identity) {
        return entityRepository.existsById(identity.getCommentId());
    }

    @Override
    public List<CommentRead> findAll() {
        return entityRepository.findAllWithUser().stream()
                .map(this::toCommentRead)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentRead> findByTargetTypeAndTargetId(TargetType targetType, Long targetId) {
        return entityRepository.findByTargetTypeAndTargetIdWithUser(targetType, targetId).stream()
                .map(this::toCommentRead)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentRead> findByTargetTypeAndTargetIdWithPaging(TargetType targetType, Long targetId, int offset, int limit) {
        return entityRepository.findByTargetTypeAndTargetIdWithPagingAndUser(targetType, targetId, offset, limit).stream()
                .map(this::toCommentRead)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentRead> findByUserId(UserIdentity userIdentity, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return entityRepository.findByUserId(userIdentity.getUserId(), pageable)
                .stream()
                .map(this::toCommentRead)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentRead> findByParentId(Long parentId) {
        return entityRepository.findByParentIdWithUser(parentId).stream()
                .map(this::toCommentRead)
                .collect(Collectors.toList());
    }

    @Override
    public Integer findMaxCommentOrder(TargetType targetType, Long targetId) {
        return entityRepository.findMaxCommentOrder(targetType, targetId);
    }

    @Override
    public void incrementSortNumbersAbove(TargetType targetType, Long targetId, Integer commentOrder, Integer sortNumber) {
        entityRepository.incrementSortNumbersAbove(targetType, targetId, commentOrder, sortNumber);
    }

    @Override
    public void incrementChildCount(Long commentId) {
        entityRepository.incrementChildCount(commentId);
    }

    /**
     * Entity ↔ Domain 변환 메서드
     * Spring Data JDBC가 자동으로 embedded 객체를 처리
     */
    private Comment toDomain(CommentEntity entity) {
        return new Comment(
                entity.getId(),
                entity.getUserId(),
                entity.getContent(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getParentId(),
                entity.getCommentOrder(),
                entity.getIsHidden(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private CommentEntity toEntity(Comment domain) {
        return new CommentEntity(
                domain.getCommentId(),
                domain.getUserId(),
                domain.getContent(),
                domain.getTargetType(),
                domain.getTargetId(),
                domain.getParentId(),
                domain.getCommentOrder(),
                domain.getIsHidden(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    /**
     * CommentWithUserDto → CommentRead 변환
     */
    private CommentRead toCommentRead(CommentWithUserDto dto) {
        String content = dto.getIsHidden()
                ? "this comment has been hidden"
                : dto.getContent();

        return new CommentRead(
                dto.getId(),
                dto.getUserId(),
                dto.getNickname(),  // User nickname from JOIN
                content,
                dto.getTargetType(),
                dto.getTargetId(),
                dto.getParentId(),
                new CommentOrder(dto.getCommentOrder(), dto.getLevel(), dto.getSortNumber(), dto.getParentId(), dto.getChildCount()),
                dto.getIsHidden(),
                dto.getCreatedAt(),
                dto.getUpdatedAt()
        );
    }
}

package dev.devrunner.model.common;

import lombok.Value;

/**
 * 댓글의 계층 구조 및 순서를 관리하는 Value Object
 *
 * ORDER BY commentOrder ASC, sortNumber ASC로 계층 구조를 유지하며 조회 가능
 *
 * 예시:
 * 댓글1 (commentOrder: 1, level: 0, sortNumber: 0, childCount: 4)
 *   └ 대댓글1-1 (commentOrder: 1, level: 1, sortNumber: 1, childCount: 1)
 *       └ 대대댓글1-1-1 (commentOrder: 1, level: 2, sortNumber: 2, childCount: 0)
 *   └ 대댓글1-2 (commentOrder: 1, level: 1, sortNumber: 3, childCount: 0)
 *   └ 대댓글1-3 (commentOrder: 1, level: 1, sortNumber: 4, childCount: 0)
 * 댓글2 (commentOrder: 2, level: 0, sortNumber: 0, childCount: 0)
 */
@Value
public class CommentOrder {
    /**
     * 댓글 그룹 번호 (최상위 댓글마다 증가)
     */
    Integer commentOrder;

    /**
     * 계층 깊이 (0: 최상위 댓글, 1: 대댓글, 2: 대대댓글...)
     */
    Integer level;

    /**
     * 같은 commentOrder 내에서의 정렬 순서
     * 계층 구조를 유지하며 연속적으로 증가
     */
    Integer sortNumber;

    /**
     * 부모 댓글 ID (최상위 댓글은 null)
     */
    Long parentId;

    /**
     * 자식 댓글 개수 (모든 하위 댓글 포함)
     */
    Integer childCount;

    /**
     * 빈 CommentOrder 생성 (초기화용)
     */
    public static CommentOrder empty() {
        return new CommentOrder(0, 0, 0, null, 0);
    }

    /**
     * 최상위 댓글용 CommentOrder 생성
     */
    public static CommentOrder newRootComment(Integer commentOrder) {
        return new CommentOrder(commentOrder, 0, 0, null, 0);
    }

    /**
     * 대댓글용 CommentOrder 생성
     */
    public static CommentOrder newReply(Integer commentOrder, Integer level, Integer sortNumber, Long parentId) {
        return new CommentOrder(commentOrder, level, sortNumber, parentId, 0);
    }

    /**
     * childCount 증가
     */
    public CommentOrder incrementChildCount() {
        return new CommentOrder(commentOrder, level, sortNumber, parentId, childCount + 1);
    }

    /**
     * sortNumber 증가
     */
    public CommentOrder incrementSortNumber() {
        return new CommentOrder(commentOrder, level, sortNumber + 1, parentId, childCount);
    }
}

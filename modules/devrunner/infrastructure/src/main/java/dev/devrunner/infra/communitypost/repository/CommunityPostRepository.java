package dev.devrunner.infra.communitypost.repository;

import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.communitypost.CommunityPostRead;
import java.util.List;
import java.util.Optional;

/**
 * CommunityPost Repository 인터페이스
 *
 * 헥사고날 아키텍처에서 Port 역할을 수행하며,
 * 비즈니스 로직에서 데이터 접근을 위한 인터페이스를 정의합니다.
 */
public interface CommunityPostRepository {

    /**
     * ID로 CommunityPost 조회
     *
     * @param identity CommunityPost 식별자
     * @return CommunityPostRead 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<CommunityPostRead> findById(CommunityPostIdentity identity);

    /**
     * 모든 CommunityPost 조회
     *
     * @return CommunityPostRead 목록
     */
    List<CommunityPostRead> findAll();

    /**
     * 사용자 ID로 CommunityPost 조회
     *
     * @param userId 사용자 ID
     * @return CommunityPostRead 목록
     */
    List<CommunityPostRead> findByUserId(Long userId);

    /**
     * 회사명으로 CommunityPost 조회
     *
     * @param company 회사명
     * @return CommunityPostRead 목록
     */
    List<CommunityPostRead> findByCompany(String company);

    /**
     * 지역으로 CommunityPost 조회
     *
     * @param location 지역
     * @return CommunityPostRead 목록
     */
    List<CommunityPostRead> findByLocation(String location);

    /**
     * 여러 ID로 CommunityPost 조회 (bulk fetch, N+1 방지)
     *
     * @param identities CommunityPost 식별자 목록
     * @return CommunityPostRead 목록
     */
    List<CommunityPostRead> findByIds(List<CommunityPostIdentity> identities);

    /**
     * CommunityPost 저장 (생성/수정)
     *
     * @param communityPost 저장할 CommunityPost
     * @return 저장된 CommunityPost
     */
    CommunityPost save(CommunityPost communityPost);

    /**
     * ID로 CommunityPost 삭제
     *
     * @param identity CommunityPost 식별자
     */
    void deleteById(CommunityPostIdentity identity);

    /**
     * CommunityPost 존재 여부 확인
     *
     * @param identity CommunityPost 식별자
     * @return 존재하면 true, 없으면 false
     */
    boolean existsById(CommunityPostIdentity identity);


    void increaseViewCount(CommunityPostIdentity identity, long increment);
    /**
     * 조회수 증가 (원자적 연산)
     *
     * DB 레벨에서 조회수를 증가시켜 동시성 문제를 방지합니다.
     * UPDATE community_post SET view_count = view_count + ? WHERE community_post_id = ?
     *
     * @param identity CommunityPost 식별자
     * @param increment 증가시킬 조회수
     */
    void increaseCommentCount(CommunityPostIdentity identity, long increment);

    /**
     * 댓글수 증가 (원자적 연산)
     *
     * DB 레벨에서 댓글수를 증가시켜 동시성 문제를 방지합니다.
     * UPDATE community_posts SET comment_count = comment_count + 1 WHERE id = ?
     *
     * @param identity CommunityPost 식별자
     */
    void increaseCommentCount(CommunityPostIdentity identity);

    /**
     * 좋아요 수 증가/감소 (원자적 연산)
     *
     * DB 레벨에서 좋아요 수를 변경하여 동시성 문제를 방지합니다.
     * UPDATE community_posts SET like_count = like_count + ? WHERE id = ?
     *
     * @param identity CommunityPost 식별자
     * @param increment 증가/감소시킬 값 (양수: 증가, 음수: 감소)
     */
    void increaseLikeCount(CommunityPostIdentity identity, long increment);

    /**
     * 싫어요 수 증가/감소 (원자적 연산)
     *
     * DB 레벨에서 싫어요 수를 변경하여 동시성 문제를 방지합니다.
     * UPDATE community_posts SET dislike_count = dislike_count + ? WHERE id = ?
     *
     * @param identity CommunityPost 식별자
     * @param increment 증가/감소시킬 값 (양수: 증가, 음수: 감소)
     */
    void increaseDislikeCount(CommunityPostIdentity identity, long increment);
}

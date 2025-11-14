package dev.devrunner.service.communitypost;

import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.communitypost.dto.CommunityPostUpsertCommand;

/**
 * CommunityPost 도메인 변경 서비스 인터페이스
 *
 * CQRS 패턴의 Command 책임을 담당하며,
 * Infrastructure Repository 기반으로 변경 로직을 제공합니다.
 */
public interface CommunityPostWriter {

    /**
     * CommunityPost 저장 (생성/수정)
     *
     * @param command CommunityPost upsert command
     * @return 저장된 CommunityPost
     */
    CommunityPost upsert(CommunityPostUpsertCommand command);

    /**
     * ID로 CommunityPost 삭제
     *
     * @param requestUser 요청 사용자 Identity
     * @param identity CommunityPost 식별자
     */
    void delete(UserIdentity requestUser, CommunityPostIdentity identity);
}

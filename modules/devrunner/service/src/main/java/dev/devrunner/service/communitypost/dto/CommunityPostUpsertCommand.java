package dev.devrunner.service.communitypost.dto;

import dev.devrunner.model.communitypost.CommunityPostCategory;
import lombok.Value;

/**
 * CommunityPost 생성/수정 Command
 *
 * API에서 추출한 userId와 요청 데이터를 명시적으로 전달합니다.
 */
@Value
public class CommunityPostUpsertCommand {
    Long requestUserId;          // 명시적으로 userId 전달
    Long communityPostId;        // null이면 생성, 있으면 수정
    CommunityPostCategory category;
    String title;
    String markdownBody;
    String company;
    String location;
    Long jobId;
    Long commentId;
}

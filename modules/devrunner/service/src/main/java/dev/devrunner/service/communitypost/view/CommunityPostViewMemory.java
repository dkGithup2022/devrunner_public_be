package dev.devrunner.service.communitypost.view;

/**
 * CommunityPost 조회수 메모리 관리 인터페이스
 *
 * 조회수를 메모리에 누적하고 주기적으로 DB에 flush하여
 * DB 부하를 줄이고 성능을 향상시킵니다.
 */
public interface CommunityPostViewMemory {

    /**
     * 조회수 증가 (비동기, 논블로킹)
     *
     * 호출 스레드는 대기하지 않고 즉시 반환됩니다.
     * 실제 조회수는 메모리에 누적됩니다.
     *
     * @param communityPostId CommunityPost ID
     */
    void countUp(Long communityPostId);

    /**
     * 메모리에 누적된 조회수를 DB에 flush
     *
     * 스케줄러가 주기적으로 호출하여 일괄 처리합니다.
     */
    void flush();
}

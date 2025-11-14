package dev.devrunner.service.communitypost.impl;

import dev.devrunner.exception.communitypost.CommunityPostNotFoundException;
import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.communitypost.CommunityPostRead;
import dev.devrunner.service.communitypost.CommunityPostReader;
import dev.devrunner.service.communitypost.view.CommunityPostViewMemory;
import dev.devrunner.infra.communitypost.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CommunityPost 도메인 조회 서비스 구현체
 *
 * CQRS 패턴의 Query 책임을 구현하며,
 * Infrastructure Repository를 활용한 조회 로직을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultCommunityPostReader implements CommunityPostReader {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostViewMemory communityPostViewMemory;

    @Override
    public CommunityPostRead read(CommunityPostIdentity identity) {
        log.debug("Reading CommunityPost by id: {}", identity.getCommunityPostId());
        CommunityPostRead communityPostRead = communityPostRepository.findById(identity)
                .orElseThrow(() -> new CommunityPostNotFoundException(""));

        // 조회수 증가 (비동기)
        communityPostViewMemory.countUp(communityPostRead.getCommunityPostId());

        return communityPostRead;
    }

    @Override
    public CommunityPostRead getById(CommunityPostIdentity identity) {
        log.debug("Fetching CommunityPost by id: {}", identity.getCommunityPostId());
        return communityPostRepository.findById(identity)
                .orElseThrow(() -> new CommunityPostNotFoundException(""));
    }

    @Override
    public List<CommunityPostRead> getAll() {
        log.debug("Fetching all CommunityPosts");
        return communityPostRepository.findAll();
    }

    @Override
    public List<CommunityPostRead> getByUserId(Long userId) {
        log.debug("Fetching CommunityPosts by userId: {}", userId);
        return communityPostRepository.findByUserId(userId);
    }

    @Override
    public List<CommunityPostRead> getByCompany(String company) {
        log.debug("Fetching CommunityPosts by company: {}", company);
        return communityPostRepository.findByCompany(company);
    }

    @Override
    public List<CommunityPostRead> getByLocation(String location) {
        log.debug("Fetching CommunityPosts by location: {}", location);
        return communityPostRepository.findByLocation(location);
    }

    @Override
    public List<CommunityPostRead> getByIds(List<CommunityPostIdentity> identities) {
        log.debug("Fetching CommunityPosts by ids: {}", identities.size());
        return communityPostRepository.findByIds(identities);
    }
}

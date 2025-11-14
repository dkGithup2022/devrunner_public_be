package dev.devrunner.service.techblog.impl;

import dev.devrunner.exception.techblog.TechBlogNotFoundException;
import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.model.techblog.TechBlogIdentity;
import dev.devrunner.service.techblog.TechBlogReader;
import dev.devrunner.service.techblog.view.TechBlogViewMemory;
import dev.devrunner.infra.techblog.repository.TechBlogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * TechBlog 도메인 조회 서비스 구현체
 *
 * CQRS 패턴의 Query 책임을 구현하며,
 * Infrastructure Repository를 활용한 조회 로직을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultTechBlogReader implements TechBlogReader {

    private final TechBlogRepository techBlogRepository;
    private final TechBlogViewMemory techBlogViewMemory;

    @Override
    public TechBlog read(TechBlogIdentity identity) {
        log.debug("Reading TechBlog by id: {}", identity.getTechBlogId());
        TechBlog techBlog = techBlogRepository.findById(identity)
                .orElseThrow(() -> new TechBlogNotFoundException("TechBlog not found: " + identity.getTechBlogId()));

        // 조회수 증가 (비동기)
        techBlogViewMemory.countUp(techBlog.getTechBlogId());

        return techBlog;
    }

    @Override
    public TechBlog getById(TechBlogIdentity identity) {
        log.debug("Fetching TechBlog by id: {}", identity.getTechBlogId());
        return techBlogRepository.findById(identity)
                .orElseThrow(() -> new TechBlogNotFoundException("TechBlog not found: " + identity.getTechBlogId()));
    }

    @Override
    public List<TechBlog> getByIds(List<TechBlogIdentity> identities) {
        log.debug("Fetching TechBlogs by ids: {}", identities);
        return techBlogRepository.findByIdsIn(identities);
    }

    @Override
    public List<TechBlog> getAll() {
        log.debug("Fetching all TechBlogs");
        return techBlogRepository.findAll();
    }

    @Override
    public Optional<TechBlog> findByUrl(String url) {
        log.debug("Fetching TechBlog by url: {}", url);
        return techBlogRepository.findByUrl(url);
    }

    @Override
    public List<TechBlog> getByCompany(String company) {
        log.debug("Fetching TechBlogs by company: {}", company);
        return techBlogRepository.findByCompany(company);
    }
}

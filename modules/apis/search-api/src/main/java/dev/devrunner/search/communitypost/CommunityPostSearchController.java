package dev.devrunner.search.communitypost;

import dev.devrunner.elasticsearch.api.communitypost.CommunityPostSearch;
import dev.devrunner.elasticsearch.api.communitypost.CommunityPostSearchResult;
import dev.devrunner.elasticsearch.document.fieldSpec.communitypost.CommunityPostIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchCommand;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import dev.devrunner.infra.user.repository.UserRepository;
import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.search.communitypost.dto.CommunityPostSearchRequest;
import dev.devrunner.search.communitypost.dto.CommunityPostSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CommunityPost Search API Controller
 */
@Tag(name = "CommunityPost Search", description = "커뮤니티 포스트 검색 API")
@RestController
@RequestMapping("/api/communityposts")
@RequiredArgsConstructor
@Slf4j
public class CommunityPostSearchController {

    private final CommunityPostSearch communityPostSearch;
    private final UserRepository userRepository;

    /**
     * Search community posts with filters
     *
     * @param request search conditions and filters
     * @return search results with pagination
     */
    @Operation(summary = "조건 기반 커뮤니티 포스트 검색")
    @PostMapping("/search")
    public ResponseEntity<CommunityPostSearchResponse> search(@RequestBody CommunityPostSearchRequest request) {
        log.info("🔍 CommunityPost search request: {}", request);

        // Build SearchCommand using Mapper
        List<SearchElement<CommunityPostIndexField>> elements = CommunityPostSearchRequestMapper.toElements(request);
        SearchCommand<CommunityPostIndexField> command = SearchCommand.of(
                elements,
                request.getFrom(),
                request.getTo()
        );

        // Execute search
        CommunityPostSearchResult result = communityPostSearch.search(command);

        // Fetch user nicknames
        List<Long> userIds = result.docs().stream()
                .map(doc -> doc.getUserId())
                .distinct()
                .toList();

        List<UserIdentity> userIdentities = userIds.stream()
                .map(UserIdentity::new)
                .toList();

        List<User> users = userRepository.findAllByIdIn(userIdentities);

        Map<Long, String> userIdToNickname = users.stream()
                .collect(Collectors.toMap(
                        user -> user.getUserId(),
                        User::getNickname
                ));

        // Convert response (CommunityPostDoc -> CommunityPostCard with nickname)
        CommunityPostSearchResponse response = CommunityPostSearchResponse.from(result, userIdToNickname);

        log.info("✅ CommunityPost search completed: found {} results", result.docs().size());
        return ResponseEntity.ok(response);
    }
}

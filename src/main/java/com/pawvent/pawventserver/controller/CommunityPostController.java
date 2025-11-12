package com.pawvent.pawventserver.controller;

import com.pawvent.pawventserver.domain.CommunityPost;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.PostCategory;
import com.pawvent.pawventserver.dto.ApiResponse;
import com.pawvent.pawventserver.dto.community.CommunityPostDtos;
import com.pawvent.pawventserver.service.CommunityPostService;
import com.pawvent.pawventserver.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService communityPostService;
    private final UserService userService;

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<CommunityPostDtos.Response>> create(
            @Valid @RequestBody CommunityPostDtos.CreateRequest request,
            Authentication authentication) {
        log.info("게시글 생성 요청: title={}, content={}, category={}, imageUrl={}, videoUrl={}", 
                request.getTitle(), request.getContent(), request.getCategory(), request.getImageUrl(), request.getVideoUrl());
        User currentUser = userService.getCurrentUser(authentication);
        String nickname = currentUser.getNickname();
        CommunityPost created = communityPostService.create(currentUser, request.getTitle(), request.getContent(), request.getCategory(), request.getImageUrl(), request.getVideoUrl());
        CommunityPostDtos.Response response = CommunityPostDtos.Response.from(communityPostService.getByIdWithUser(created.getId()), nickname);
        return ResponseEntity.ok(ApiResponse.success("게시글이 등록되었습니다.", response));
    }

    @GetMapping
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public ResponseEntity<ApiResponse<List<CommunityPostDtos.Response>>> list(
            @RequestParam(value = "category", required = false) PostCategory category,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        Page<CommunityPost> result = communityPostService.list(category, page, size);
        List<CommunityPostDtos.Response> responses = result.getContent().stream()
                .map(p -> CommunityPostDtos.Response.from(p, p.getUser().getNickname()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("게시글 목록을 조회했습니다.", responses));
    }

    @GetMapping("/{postId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<CommunityPostDtos.Response>> get(
            @PathVariable("postId") Long postId
    ) {
        CommunityPost post = communityPostService.getByIdWithUser(postId);
        CommunityPostDtos.Response response = CommunityPostDtos.Response.from(post, post.getUser().getNickname());
        return ResponseEntity.ok(ApiResponse.success("게시글을 조회했습니다.", response));
    }

    @PutMapping("/{postId}")
    @Transactional
    public ResponseEntity<ApiResponse<CommunityPostDtos.Response>> update(
            @PathVariable("postId") Long postId,
            @Valid @RequestBody CommunityPostDtos.UpdateRequest request,
            Authentication authentication
    ) {
        User currentUser = userService.getCurrentUser(authentication);
        CommunityPost updated = communityPostService.update(
                postId,
                currentUser,
                request.getTitle(),
                request.getContent(),
                request.getCategory(),
                request.getImageUrl(),
                request.getVideoUrl()
        );
        CommunityPostDtos.Response response = CommunityPostDtos.Response.from(
                communityPostService.getByIdWithUser(updated.getId()),
                currentUser.getNickname()
        );
        return ResponseEntity.ok(ApiResponse.success("게시글이 수정되었습니다.", response));
    }

    @DeleteMapping("/{postId}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("postId") Long postId,
            Authentication authentication
    ) {
        User currentUser = userService.getCurrentUser(authentication);
        communityPostService.delete(postId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다.", null));
    }
}



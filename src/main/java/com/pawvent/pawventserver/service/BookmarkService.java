package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.Bookmark;
import com.pawvent.pawventserver.domain.Hazard;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {
    
    private final BookmarkRepository bookmarkRepository;
    
    /**
     * 새로운 북마크를 생성합니다.
     * 사용자가 위험요소를 북마크하여 나중에 쉽게 찾을 수 있도록 합니다.
     * 동일한 사용자가 같은 위험요소를 중복 북마크할 수 없습니다.
     * 
     * @param user 북마크를 생성하는 사용자
     * @param hazard 북마크할 위험요소
     * @return 생성된 북마크 엔티티
     * @throws IllegalArgumentException 이미 북마크한 위험요소인 경우
     */
    @Transactional
    public Bookmark createBookmark(User user, Hazard hazard) {
        // 이미 북마크했는지 확인
        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndHazard(user, hazard);
        if (existingBookmark.isPresent()) {
            throw new IllegalArgumentException("이미 북마크한 위험요소입니다.");
        }
        
        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .hazard(hazard)
                .build();
        
        return bookmarkRepository.save(bookmark);
    }
    
    @Transactional
    public void deleteBookmark(User user, Hazard hazard) {
        Bookmark bookmark = bookmarkRepository.findByUserAndHazard(user, hazard)
                .orElseThrow(() -> new IllegalArgumentException("북마크를 찾을 수 없습니다."));
        
        bookmarkRepository.delete(bookmark);
    }
    
    @Transactional
    public void deleteBookmarkById(Long bookmarkId, User user) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new IllegalArgumentException("북마크를 찾을 수 없습니다."));
        
        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        
        bookmarkRepository.delete(bookmark);
    }
    
    /**
     * 특정 사용자의 모든 북마크를 조회합니다.
     * 사용자가 북마크한 위험요소들을 최신순으로 보여줍니다.
     * 
     * @param user 북마크를 조회할 사용자
     * @return 해당 사용자의 북마크 목록
     */
    public List<Bookmark> getUserBookmarks(User user) {
        return bookmarkRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public boolean isBookmarked(User user, Hazard hazard) {
        return bookmarkRepository.existsByUserAndHazard(user, hazard);
    }
    
    public List<Hazard> getUserBookmarkedHazards(User user) {
        return bookmarkRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(Bookmark::getHazard)
                .toList();
    }
    
    public long getBookmarkCount(Hazard hazard) {
        return bookmarkRepository.countByHazard(hazard);
    }
    
    public long getUserBookmarkCount(User user) {
        return bookmarkRepository.countByUser(user);
    }
    
    public List<Hazard> getPopularHazards() {
        return bookmarkRepository.findPopularHazards()
                .stream()
                .map(result -> (Hazard) result[0])
                .toList();
    }
    
    @Transactional
    public Bookmark toggleBookmark(User user, Hazard hazard) {
        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndHazard(user, hazard);
        
        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
            return null; // 북마크 해제됨
        } else {
            return createBookmark(user, hazard);
        }
    }
}

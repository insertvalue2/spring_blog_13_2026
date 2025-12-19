package org.example.demo_ssr_v1_1.board;

import lombok.Data;

import org.example.demo_ssr_v1_1._core.utils.MyDateUtil;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * 게시글 응답 DTO
 * 
 * Open Session in View가 false일 때:
 * - 트랜잭션이 끝나면 세션이 종료되어 LAZY 로딩 불가
 * - Service에서 필요한 데이터를 모두 조회하고 DTO로 변환하여 반환
 * - 엔티티를 직접 반환하지 않고 DTO를 반환하여 계층 간 결합도 감소
 */
public class BoardResponse {

    /**
     * 게시글 목록 응답 DTO
     */
    @Data
    public static class ListDTO {
        private Long id;
        private String title;
        private String username;  // 작성자명
        private String createdAt; // 포맷된 생성일

        public ListDTO(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            // JOIN FETCH로 이미 로딩된 user 사용 (추가 쿼리 없음)
            if (board.getUser() != null) {
                this.username = board.getUser().getUsername();
            }
            // 날짜 포맷팅
            if (board.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(board.getCreatedAt());
            }
        }
    }

    /**
     * 게시글 상세 응답 DTO
     */
    @Data
    public static class DetailDTO {
        private Long id;
        private String title;
        private String content;
        private Long userId;      // 작성자 ID
        private String username;  // 작성자명
        private String createdAt; // 포맷된 생성일

        public DetailDTO(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
            // JOIN FETCH로 이미 로딩된 user 사용 (추가 쿼리 없음)
            if (board.getUser() != null) {
                this.userId = board.getUser().getId();
                this.username = board.getUser().getUsername();
            }
            // 날짜 포맷팅
            if (board.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(board.getCreatedAt());
            }
        }
    }

    /**
     * 게시글 수정 화면 응답 DTO
     */
    @Data
    public static class UpdateFormDTO {
        private Long id;
        private String title;
        private String content;
        private String username;  // 작성자명 (평탄화)

        public UpdateFormDTO(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
            // JOIN FETCH로 이미 로딩된 user 사용 (추가 쿼리 없음)
            if (board.getUser() != null) {
                this.username = board.getUser().getUsername();
            }
        }
    }

    /**
     * 페이징 정보 DTO
     * 
     * Spring Data JPA의 Page 객체를 View에 전달하기 위한 DTO
     * 페이징 정보와 페이지 링크 정보를 포함합니다.
     */
    @Data
    public static class PageDTO {
        private List<ListDTO> content;  // 현재 페이지의 게시글 목록
        private int number;              // 현재 페이지 번호 (0부터 시작)
        private int size;                // 페이지 크기
        private int totalPages;          // 전체 페이지 수
        private long totalElements;      // 전체 게시글 수
        private boolean first;           // 첫 페이지 여부
        private boolean last;            // 마지막 페이지 여부
        private boolean hasNext;         // 다음 페이지 존재 여부
        private boolean hasPrevious;     // 이전 페이지 존재 여부
        private Integer previousPageNumber;  // 이전 페이지 번호 (없으면 null)
        private Integer nextPageNumber;      // 다음 페이지 번호 (없으면 null)
        private List<PageLink> pageLinks;    // 페이지 번호 링크 목록

        /**
         * PageDTO 생성자
         * 
         * @param page Spring Data JPA의 Page<Board> 객체
         */
        public PageDTO(Page<Board> page) {
            // 게시글 목록을 DTO로 변환
            this.content = page.getContent().stream()
                    .map(ListDTO::new)
                    .toList();
            
            // 페이징 정보 설정
            this.number = page.getNumber();           // 현재 페이지 번호 (0부터 시작)
            this.size = page.getSize();               // 페이지 크기
            this.totalPages = page.getTotalPages();   // 전체 페이지 수
            this.totalElements = page.getTotalElements(); // 전체 게시글 수
            this.first = page.isFirst();              // 첫 페이지 여부
            this.last = page.isLast();                // 마지막 페이지 여부
            this.hasNext = page.hasNext();            // 다음 페이지 존재 여부
            this.hasPrevious = page.hasPrevious();    // 이전 페이지 존재 여부
            
            // 이전/다음 페이지 번호 설정 (1부터 시작하는 번호로 변환)
            // page.getNumber()는 0부터 시작하므로 1부터 시작하는 번호로 변환
            // 예: page.getNumber() = 0 (첫 페이지) -> previousPageNumber = null
            //     page.getNumber() = 1 (두 번째 페이지) -> previousPageNumber = 1 (1페이지로 이동)
            this.previousPageNumber = page.hasPrevious() ? page.getNumber() : null;
            // 예: page.getNumber() = 0 (첫 페이지) -> nextPageNumber = 2 (2페이지로 이동)
            //     page.getNumber() = 1 (두 번째 페이지) -> nextPageNumber = 3 (3페이지로 이동)
            this.nextPageNumber = page.hasNext() ? page.getNumber() + 2 : null;
            
            // 페이지 링크 생성 (현재 페이지 기준 앞뒤 2페이지씩 표시)
            this.pageLinks = generatePageLinks(page);
        }

        /**
         * 페이지 링크 생성
         * 
         * 현재 페이지를 기준으로 앞뒤 2페이지씩 표시합니다.
         * 예: 현재 페이지가 5이면 [3, 4, 5, 6, 7] 표시
         * 
         * @param page Spring Data JPA의 Page 객체
         * @return 페이지 링크 목록
         */
        private List<PageLink> generatePageLinks(Page<Board> page) {
            List<PageLink> links = new ArrayList<>();
            
            int currentPage = page.getNumber() + 1;  // 0부터 시작하는 번호를 1부터 시작하는 번호로 변환
            int totalPages = page.getTotalPages();
            
            // 시작 페이지와 끝 페이지 계산
            int startPage = Math.max(1, currentPage - 2);
            int endPage = Math.min(totalPages, currentPage + 2);
            
            // 페이지 링크 생성
            for (int i = startPage; i <= endPage; i++) {
                PageLink link = new PageLink();
                link.setDisplayNumber(i);
                link.setActive(i == currentPage);
                links.add(link);
            }
            
            return links;
        }
    }

    /**
     * 페이지 링크 정보 DTO
     */
    @Data
    public static class PageLink {
        private int displayNumber;  // 표시할 페이지 번호 (1부터 시작)
        private boolean active;     // 현재 페이지 여부
    }
}


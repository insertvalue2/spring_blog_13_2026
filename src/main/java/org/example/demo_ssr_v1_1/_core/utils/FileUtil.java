package org.example.demo_ssr_v1_1._core.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 파일 업로드 유틸리티 클래스
 * 
 * 파일 저장 및 관리 기능을 제공합니다.
 * 
 * 핵심 개념:
 * 1. 유틸리티 클래스: 정적 메서드만 제공하는 클래스
 * 2. 파일 저장: UUID를 사용하여 파일명 중복 방지
 * 3. 디렉토리 관리: 업로드 디렉토리 자동 생성
 * 4. 파일 검증: 이미지 파일 여부 확인
 */
public class FileUtil {

    // 업로드된 파일을 저장할 디렉토리 경로 상수
    public static final String IMAGES_DIR = "images/";  // 프로필 이미지 저장 경로

    /**
     * 파일을 저장하고 저장된 파일명을 반환합니다. (기본 디렉토리: images/)
     * 
     * 하위 호환성을 위한 메서드입니다.
     * 프로필 이미지 저장 시 사용됩니다.
     * 
     * @param file 업로드된 파일
     * @return 저장된 파일명 (UUID + 원본 파일명)
     * @throws IOException 파일 저장 실패 시
     */
    public static String saveFile(MultipartFile file) throws IOException {
        return saveFile(file, IMAGES_DIR);
    }

    /**
     * 파일을 저장하고 저장된 파일명을 반환합니다.
     * 
     * 파일 업로드 핵심 개념:
     * 1. MultipartFile: 브라우저에서 전송된 파일 데이터를 담는 객체
     * 2. 파일은 서버 디스크에 저장되고, DB에는 파일명만 저장됨
     * 3. UUID를 사용하여 파일명 중복 방지
     * 
     * 파일 저장 과정:
     * 1. 파일 유효성 검사 (null, empty 체크)
     * 2. 업로드 디렉토리 생성 (없으면)
     * 3. UUID를 사용한 고유 파일명 생성
     * 4. 파일을 디스크에 저장
     * 
     * @param file 업로드된 파일 (MultipartFile 객체)
     * @param uploadDir 저장할 디렉토리 경로 (예: "images/", "files/")
     * @return 저장된 파일명 (UUID + 원본 파일명, 예: "abc123-456-789-profile.jpg")
     *         파일이 없으면 null 반환
     * @throws IOException 파일 저장 실패 시 (디스크 공간 부족, 권한 없음 등)
     */
    public static String saveFile(MultipartFile file, String uploadDir) throws IOException {
        // 1단계: 파일 유효성 검사
        // MultipartFile.isEmpty(): 파일이 없거나 크기가 0이면 true
        if (file == null || file.isEmpty()) {
            return null;  // 파일이 없으면 null 반환 (선택사항이므로 에러 아님)
        }

        // 2단계: 업로드 디렉토리 생성
        // Path: 파일 시스템 경로를 나타내는 객체
        // Paths.get(): 문자열 경로를 Path 객체로 변환
        Path uploadPath = Paths.get(uploadDir);
        
        // 디렉토리가 존재하지 않으면 생성
        // Files.exists(): 파일/디렉토리 존재 여부 확인
        // Files.createDirectories(): 디렉토리 생성 (상위 디렉토리도 자동 생성)
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 3단계: 원본 파일명 가져오기
        // getOriginalFilename(): 사용자가 업로드한 원본 파일명 (예: "profile.jpg")
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IOException("파일명이 없습니다");
        }
        
        // 4단계: UUID를 사용한 고유 파일명 생성
        //   왜 UUID를 사용하나요?
        // - 같은 이름의 파일을 여러 사용자가 업로드할 수 있음
        // - 예: "profile.jpg"를 100명이 업로드하면 덮어쓰기 발생!
        // - UUID를 앞에 붙이면: "abc123-456-789-profile.jpg" (고유함)
        // 
        // UUID.randomUUID(): 랜덤한 UUID 생성 (예: "550e8400-e29b-41d4-a716-446655440000")
        String uuid = UUID.randomUUID().toString();
        String savedFilename = uuid + "_" + originalFilename;
        // 결과 예시: "550e8400-e29b-41d4-a716-446655440000_profile.jpg"

        // 5단계: 파일을 디스크에 저장
        // uploadPath.resolve(savedFilename): 경로와 파일명을 결합
        //   예: "images/" + "550e8400-..._profile.jpg" = "images/550e8400-..._profile.jpg"
        Path filePath = uploadPath.resolve(savedFilename);
        
        // Files.copy(): 파일 복사 (MultipartFile의 InputStream → 디스크 파일)
        // file.getInputStream(): 업로드된 파일의 입력 스트림
        // filePath: 저장할 파일 경로
        Files.copy(file.getInputStream(), filePath);

        // 저장된 파일명 반환 (DB에 저장할 파일명)
        //  주의: 전체 경로가 아닌 파일명만 반환!
        //   반환: "550e8400-..._profile.jpg" (O)
        //   반환: "images/550e8400-..._profile.jpg" (X) - 경로는 WebMvcConfig에서 처리
        return savedFilename;
    }

    /**
     * 파일을 삭제합니다. (기본 디렉토리: images/)
     * 
     * 하위 호환성을 위한 메서드입니다.
     * 
     * @param filename 삭제할 파일명
     * @throws IOException 파일 삭제 실패 시
     */
    public static void deleteFile(String filename) throws IOException {
        deleteFile(filename, IMAGES_DIR);
    }

    /**
     * 파일을 삭제합니다.
     * 
     * 파일 삭제가 필요한 경우:
     * 1. 프로필 이미지 수정 시: 기존 이미지 삭제 (디스크 공간 절약)
     * 2. 프로필 이미지 삭제 시: 파일 삭제
     * 3. 게시글 삭제 시: 첨부파일 삭제 (향후 구현)
     * 
     *  주의: DB에서만 삭제하면 안 됨!
     * - DB의 파일명만 삭제하면 디스크에 사용하지 않는 파일이 계속 쌓임
     * - 반드시 디스크의 실제 파일도 삭제해야 함
     * 
     * @param filename 삭제할 파일명 (예: "550e8400-..._profile.jpg")
     * @param uploadDir 파일이 저장된 디렉토리 경로 (예: "images/")
     * @throws IOException 파일 삭제 실패 시
     */
    public static void deleteFile(String filename, String uploadDir) throws IOException {
        // 파일명이 없으면 삭제할 것이 없으므로 종료
        if (filename == null || filename.isEmpty()) {
            return;
        }

        // 삭제할 파일의 전체 경로 생성
        // Paths.get(uploadDir, filename): 경로와 파일명 결합
        //   예: "images/" + "550e8400-..._profile.jpg" = "images/550e8400-..._profile.jpg"
        Path filePath = Paths.get(uploadDir, filename);
        
        // 파일이 존재하는지 확인 후 삭제
        //  Files.delete()는 파일이 없으면 예외 발생!
        // → Files.exists()로 먼저 확인 필요
        if (Files.exists(filePath)) {
            Files.delete(filePath);  // 실제 디스크에서 파일 삭제
        }
        // 파일이 없으면 그냥 종료 (이미 삭제된 상태)
    }

    /**
     * 파일이 이미지인지 확인합니다.
     * 
     * 파일 타입 검증이 왜 필요한가요?
     * - 보안: 악성 파일 업로드 방지 (예: .exe, .sh 파일)
     * - 데이터 무결성: 이미지만 허용하는 경우 다른 파일 타입 차단
     * 
     * 주의: HTML의 accept="image/*"만으로는 보안이 보장되지 않음!
     * - 사용자가 브라우저 개발자 도구로 우회 가능
     * - 반드시 서버에서도 검증해야 함 (현재 메서드)
     * 
     * 검증 방법:
     * - Content-Type 확인: 파일의 MIME 타입 (예: "image/jpeg", "image/png")
     * - "image/"로 시작하면 이미지 파일로 판단
     * 
     * @param file 업로드된 파일
     * @return 이미지 파일이면 true, 아니면 false
     */
    public static boolean isImageFile(MultipartFile file) {
        // 파일이 없으면 이미지가 아님
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Content-Type 가져오기
        // 예: "image/jpeg", "image/png", "image/gif"
        // 예: "application/pdf" (이미지 아님)
        String contentType = file.getContentType();
        
        // Content-Type이 "image/"로 시작하는지 확인
        // startsWith("image/"): "image/jpeg", "image/png" 등은 true
        return contentType != null && contentType.startsWith("image/");
    }

    /* =======================================================================
     * 이하 코드는 향후 "게시글 첨부파일" 기능 구현 시 활성화 예정입니다.
     * 현재 프로필 이미지 기능에는 사용되지 않으므로 주석으로 보관합니다.
     * ======================================================================= */

    // public static final String FILES_DIR = "files/"; // 게시글 첨부파일 저장 경로 (향후 사용)

    // /**
    //  * 여러 파일을 저장하고 저장된 파일명 리스트를 반환합니다.
    //  * (게시글 첨부파일 N개 등록용 예정)
    //  */
    // public static List<String> saveFiles(List<MultipartFile> files, String uploadDir) throws IOException {
    //     List<String> savedFilenames = new ArrayList<>();
    //     if (files == null || files.isEmpty()) return savedFilenames;
    //     for (MultipartFile file : files) {
    //         if (file != null && !file.isEmpty()) {
    //             String savedFilename = saveFile(file, uploadDir);
    //             if (savedFilename != null) savedFilenames.add(savedFilename);
    //         }
    //     }
    //     return savedFilenames;
    // }

    // /**
    //  * 여러 파일을 삭제합니다. (게시글 첨부파일 삭제용 예정)
    //  */
    // public static void deleteFiles(List<String> filenames, String uploadDir) throws IOException {
    //     if (filenames == null || filenames.isEmpty()) return;
    //     for (String filename : filenames) {
    //         deleteFile(filename, uploadDir);
    //     }
    // }
}


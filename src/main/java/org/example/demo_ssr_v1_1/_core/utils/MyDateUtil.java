package org.example.demo_ssr_v1_1._core.utils;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class MyDateUtil {

    // 정적 변수로 포맷터를 선언해두면 성능상 더 유리합니다.
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String timestampFormat(Timestamp time) {
        if (time == null) {
            return null;
        }
        // Timestamp -> LocalDateTime 변환 후 포맷 적용
        return time.toLocalDateTime().format(FORMATTER);
    }
}

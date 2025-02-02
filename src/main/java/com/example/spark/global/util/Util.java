package com.example.spark.global.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import java.time.LocalDate;
import java.util.List;

public class Util {

    public static HttpEntity<String> createHttpEntity(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return new HttpEntity<>(headers);
    }

    public static List<DateRange> calculateDateRanges() {
        LocalDate today = LocalDate.now();
        return List.of(
                new DateRange(today.minusDays(30).toString(), today.toString()),  // 최근 30일
                new DateRange(today.minusDays(60).toString(), today.minusDays(30).toString()),  // 최근 30~60일
                new DateRange(today.minusDays(90).toString(), today.minusDays(60).toString())  // 최근 60~90일
        );
    }
}

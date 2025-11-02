package com.pawvent.pawventserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * API 공통 응답 DTO
 * 
 * 모든 API 응답의 표준 형식을 정의하는 제네릭 클래스입니다.
 * 성공/실패 여부, 메시지, 데이터를 일관된 형태로 전달합니다.
 * 
 * @param <T> 응답 데이터의 타입
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    /** 요청 성공 여부 */
    private boolean success;
    
    /** 응답 메시지 */
    private String message;
    
    /** 응답 데이터 */
    private T data;
    
    /**
     * 성공 응답 생성 (기본 메시지)
     * @param data 응답 데이터
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }
    
    /**
     * 성공 응답 생성 (커스텀 메시지)
     * @param message 응답 메시지
     * @param data 응답 데이터
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    /**
     * 에러 응답 생성
     * @param message 에러 메시지
     * @return 에러 응답 객체
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}

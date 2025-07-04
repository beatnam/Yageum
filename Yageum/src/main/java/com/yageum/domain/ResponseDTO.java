package com.yageum.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDTO<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ResponseDTO<T> success(String message, T data) {
        return ResponseDTO.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ResponseDTO<T> fail(String message) {
        return ResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}
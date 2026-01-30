package com.crcs.authservice.dto;

import java.time.LocalDateTime;

public class ApiResponseDTO {
    private String message;
    private LocalDateTime timestamp;
    private Object data;

    public ApiResponseDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponseDTO(String message) {
        this();
        this.message = message;
    }

    public ApiResponseDTO(String message, Object data) {
        this();
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

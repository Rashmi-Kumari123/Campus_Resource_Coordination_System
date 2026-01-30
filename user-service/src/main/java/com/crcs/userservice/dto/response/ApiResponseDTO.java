package com.crcs.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO {
    private String message;
    private LocalDateTime timestamp;
    private Object data;

    public ApiResponseDTO(String message) {
        this();
        this.message = message;
    }

    public ApiResponseDTO(String message, Object data) {
        this();
        this.message = message;
        this.data = data;
    }

    private void init() {
        this.timestamp = LocalDateTime.now();
    }
}

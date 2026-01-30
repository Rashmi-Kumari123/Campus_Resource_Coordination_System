package com.crcs.userservice.exception;

import com.crcs.userservice.dto.response.ApiResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseDTO> handleUserNotFoundException(UserNotFoundException e) {
        logger.error("User not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.error("Invalid argument: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponseDTO> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        logger.error("Unsupported media type: {}", e.getMessage());
        String message = "Request body must be JSON. Set Content-Type header to 'application/json' and send a valid JSON body.";
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ApiResponseDTO(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDTO> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        logger.error("Invalid request body: {}", e.getMessage());
        String message = "Invalid request body. Ensure Content-Type is 'application/json' and the body is valid JSON.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseDTO(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO> handleGenericException(Exception e) {
        logger.error("Unexpected error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO("An unexpected error occurred"));
    }
}

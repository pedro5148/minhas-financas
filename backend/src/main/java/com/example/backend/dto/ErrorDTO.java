package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ErrorDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private List<FieldErrorDTO> fieldErrors;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class FieldErrorDTO {
        private String field;
        private String message;
    }
}

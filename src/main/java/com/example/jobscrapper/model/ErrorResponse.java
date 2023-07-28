package com.example.jobscrapper.model;

import lombok.Data;

@Data
public class ErrorResponse {
    private int status;
    private String message;
}

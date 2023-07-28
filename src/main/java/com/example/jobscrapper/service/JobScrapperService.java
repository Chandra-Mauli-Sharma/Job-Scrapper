package com.example.jobscrapper.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Component
public interface JobScrapperService {
    ResponseEntity<?> getJobs(Optional<String> company);
    ResponseEntity<?> getJobFromDb(Optional<String> companyname, Optional<String> jobtype);
}

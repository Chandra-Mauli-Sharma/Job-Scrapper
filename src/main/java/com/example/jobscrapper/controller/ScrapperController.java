package com.example.jobscrapper.controller;

import com.example.jobscrapper.service.JobScrapperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class ScrapperController {

    final JobScrapperService jobScrapperService;

    public ScrapperController(JobScrapperService jobScrapperService) {
        this.jobScrapperService = jobScrapperService;
    }

    @GetMapping("/scrap")
    ResponseEntity<?> getJobs(@RequestParam("sitename") Optional<String> sitename){
        return jobScrapperService.getJobs(sitename);
    }

    @GetMapping("/getJobFromDb")
    ResponseEntity<?> getJobFromDb(@RequestParam("companyname") Optional<String> companyname,@RequestParam("jobtype") Optional<String> jobtype){
        return jobScrapperService.getJobFromDb(companyname, jobtype);
    }
}

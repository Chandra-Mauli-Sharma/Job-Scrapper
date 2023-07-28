package com.example.jobscrapper.service.impl;

import com.example.jobscrapper.model.ErrorResponse;
import com.example.jobscrapper.model.JobModel;
import com.example.jobscrapper.repository.JobScrapperRepository;
import com.example.jobscrapper.service.JobScrapperService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JobScrapperServiceImpl implements JobScrapperService {

    final JobScrapperRepository jobScrapperRepository;
    protected WebDriver driver;

    public JobScrapperServiceImpl(JobScrapperRepository jobScrapperRepository) throws MalformedURLException {
        this.jobScrapperRepository = jobScrapperRepository;
        setupWebDriver();
    }

    @Override
    public ResponseEntity<?> getJobs(Optional<String> sitename) {
        try {
            List<JobModel> jobList=new ArrayList<>();
            if(sitename.isPresent()){
                switch (sitename.get()) {
                    case "lever" -> {
                        jobList.addAll(fetchMercedes());
                    }
                    case "airbnb" -> {
                        jobList.addAll(fetchAirbnb());
                    }
                    case "meta" -> {
                        jobList.addAll(fetchMeta());
                    }
                }
            } else{
                jobList.addAll(fetchMercedes());
                jobList.addAll(fetchAirbnb());
                jobList.addAll(fetchMeta());
            }
            return new ResponseEntity<>(Map.of("status", HttpStatus.OK.value(), "data", jobList.subList(0,11)), HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage(e.getMessage());
            errorResponse.setStatus(HttpStatus.CONFLICT.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }
    }

    @Override
    public ResponseEntity<?> getJobFromDb(Optional<String> companyname, Optional<String> jobtype) {
        try {
            List<JobModel> jobList=new ArrayList<>();
            if(companyname.isPresent()&&jobtype.isPresent()){
                jobList.addAll(jobScrapperRepository.findByCompanyNameEqualsIgnoreCaseAndTypeEqualsIgnoreCase(companyname.get(),jobtype.get()));
            }
            return new ResponseEntity<>(Map.of("status", HttpStatus.OK.value(), "data", jobList), HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage(e.getMessage());
            errorResponse.setStatus(HttpStatus.CONFLICT.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }
    }

    void setupWebDriver() throws MalformedURLException {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.addArguments("start-maximized");
        chromeOptions.addArguments("disable-infobars");
        chromeOptions.addArguments("--disable-extensions");
        chromeOptions.addArguments("window-size=1920x1080");
        this.driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), chromeOptions);
    }

    List<JobModel> fetchMercedes() {
        driver.get("https://jobs.lever.co/MBRDNA");
        return driver.findElements(By.className("postings-group")).stream().map(it -> {
            JobModel jobModel = new JobModel();
            jobModel.setTitle(it.findElement(By.tagName("h5")).getText());
            WebElement categories = it.findElement(By.className("posting-categories"));
            jobModel.setLocation(categories.findElement(By.className("location")).getText());
            jobModel.setDescriptionLink(it.findElement(By.className("posting-title")).getDomAttribute("href"));
            jobModel.setType(categories.findElements(By.className("commitment")).size() > 0 ? categories.findElement(By.className("commitment")).getText() : "Type Not Available");
            jobModel.setCompanyName("Mercedes-Benz");
            if (jobExistInDb(jobModel))
                return jobScrapperRepository.getJobModelByCompanyNameAndTitleAndLocationAndType(jobModel.getCompanyName(), jobModel.getTitle(), jobModel.getLocation(), jobModel.getType());
            return jobScrapperRepository.save(jobModel);
        }).toList();
    }

    List<JobModel> fetchMeta() {
        driver.get("https://www.metacareers.com/jobs/?is_leadership=0&teams%5B0%5D=Internship+-+Engineering%2C+Tech+%26+Design&teams%5B1%5D=Internship+-+Business&teams%5B2%5D=Internship+-+PhD&teams%5B3%5D=University+Grad+-+PhD+%26+Postdoc&teams%5B4%5D=University+Grad+-+Engineering%2C+Tech+%26+Design&teams%5B5%5D=University+Grad+-+Business&is_in_page=1");

        return driver.findElements(By.className("x1ypdohk")).stream().map(it -> {
            JobModel jobModel = new JobModel();
            jobModel.setTitle(it.findElement(By.className("x2izyaf")).getText());
            jobModel.setLocation(it.findElement(By.className("x7z1be2")).getText());
            jobModel.setDescriptionLink(it.findElement(By.className("x2izyaf")).getText());
            jobModel.setType("Intern");

            return jobModel;
        }).toList();
    }

    List<JobModel> fetchAirbnb() {
        driver.get("https://careers.airbnb.com/university/");
        return driver.findElement(By.className("jobs-board__departments__list")).findElements(By.className("jobs-board__departments__item")).stream().map(it -> {
            WebElement button = it.findElement(By.className("jobs-board__departments__item__button"));
            button.click();
            return driver.findElement(By.className("jobs-board__positions__list")).findElements(By.className("jobs-board__positions__list__item")).stream().map(item -> {
                JobModel jobModel = new JobModel();
                jobModel.setDescriptionLink(item.findElement(By.tagName("a")).getDomAttribute("href"));
                jobModel.setLocation(item.findElement(By.className("jobs-board__positions__list__item__location")).getText());
                jobModel.setType(button.getText().split("\\(")[0].trim());
                jobModel.setTitle(item.findElement(By.className("jobs-board__positions__list__item__title")).getText());
                jobModel.setCompanyName("Airbnb");
                if (jobExistInDb(jobModel))
                    return jobScrapperRepository.getJobModelByCompanyNameAndTitleAndLocationAndType(jobModel.getCompanyName(), jobModel.getTitle(), jobModel.getLocation(), jobModel.getType());
                return jobScrapperRepository.save(jobModel);
            }).toList();
        }).flatMap(List::stream).toList();
    }

    boolean jobExistInDb(JobModel jobModel) {
        return jobScrapperRepository.existsByCompanyNameAndTitleAndLocationAndType(jobModel.getCompanyName(), jobModel.getTitle(), jobModel.getLocation(), jobModel.getType());
    }
}

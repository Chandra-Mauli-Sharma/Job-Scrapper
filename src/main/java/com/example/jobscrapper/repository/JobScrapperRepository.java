package com.example.jobscrapper.repository;

import com.example.jobscrapper.model.JobModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface JobScrapperRepository extends JpaRepository<JobModel,Long>, PagingAndSortingRepository<JobModel,Long> {
    boolean existsByCompanyNameAndTitleAndLocationAndType(String companyName, String title, String location, String type);
    JobModel getJobModelByCompanyNameAndTitleAndLocationAndType(String companyName, String title, String location, String type);

    List<JobModel> findByCompanyNameEqualsIgnoreCaseAndTypeEqualsIgnoreCase(String companyName,String jobtype);
}

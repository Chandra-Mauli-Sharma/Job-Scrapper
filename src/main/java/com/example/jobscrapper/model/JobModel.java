package com.example.jobscrapper.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class JobModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String location;
    private String descriptionLink;
    private String type;
    private String companyName;
}


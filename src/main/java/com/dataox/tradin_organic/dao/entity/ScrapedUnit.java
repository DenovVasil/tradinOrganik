package com.dataox.tradin_organic.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrapedUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String source;
    private String companyName;
    private String country;
    private String certifiedActivities;
    private String certifiedProducts;

    public ScrapedUnit(String source, String companyName, String country) {
        this.source = source;
        this.companyName = companyName;
        this.country = country;
    }

}

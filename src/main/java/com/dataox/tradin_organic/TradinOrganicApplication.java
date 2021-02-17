package com.dataox.tradin_organic;

import com.dataox.tradin_organic.service.scraper.ImoControlLationamericaLtdaScraper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AllArgsConstructor
public class TradinOrganicApplication implements CommandLineRunner {

    ImoControlLationamericaLtdaScraper imoControlLationamericaLtdaScraper;

    public static void main(String[] args) {
        SpringApplication.run(TradinOrganicApplication.class, args);
    }

    @Override
    public void run(String... args){
        imoControlLationamericaLtdaScraper.scrap();
    }
}

package com.dataox.tradin_organic.service.scraper;

import com.dataox.tradin_organic.dao.entity.ScrapedUnit;
import com.dataox.tradin_organic.dao.repo.ScrapedUnitRepo;
import com.dataox.tradin_organic.service.JsoupConnection;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@AllArgsConstructor
public class ControlunionScraper {
    private static final String BASE_URL = "http://cucpublications.controlunion.com/publications.aspx?Subprogram_ID=1&Program_ID=1&_ga=2.99937502.1782417472.1612431599-1775221004.1597843969";
    private static final String PDF_SELECTOR = "h1:contains(Organic EU)  ~ ul li:first-child a";
    private final JsoupConnection jsoupConnection;
    private final ScrapedUnitRepo scrapedUnitRepo;
    private final ControlunionParser controlunionParser;

    @Transactional
    public void scrap(){
        Document doc = jsoupConnection.getDocument(BASE_URL);
        List<ScrapedUnit> list = doc.select(PDF_SELECTOR)
                .stream()
                .map(e -> e.attr("abs:href"))
                .map(controlunionParser::parse)
                .flatMap(List::stream)
                .collect(toList());
        scrapedUnitRepo.saveAll(list);
    }


}

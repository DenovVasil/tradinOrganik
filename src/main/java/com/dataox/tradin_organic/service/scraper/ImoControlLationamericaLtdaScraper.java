package com.dataox.tradin_organic.service.scraper;

import com.dataox.tradin_organic.dao.entity.ScrapedUnit;
import com.dataox.tradin_organic.dao.repo.ScrapedUnitRepo;
import com.dataox.tradin_organic.service.JsoupConnection;
import lombok.AllArgsConstructor;
import org.aspectj.apache.bcel.generic.RET;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

@Service
@AllArgsConstructor
public class ImoControlLationamericaLtdaScraper {

    private static final String BASE_URL = "https://imocert.bio/operadores-certificados/";
    private static final String ALL_ROWS_SELECTOR = ".row-hover > tr";
    private static final String COMPANY_SELECTOR = ".column-1";
    private static final String COUNTRY_SELECTOR = ".column-2";
    private static final String PDF_SELECTOR = ".column-3 a[href]";
    private final ImoControlLationamericaLtdaParser imoControlLationamericaLtdaParser;
    private final JsoupConnection jsoupConnection;
    private final ScrapedUnitRepo scrapedUnitRepo;

    @Transactional
    public void scrape() {
        Document doc = jsoupConnection.getDocument(BASE_URL);
        List<ScrapedUnit> list = doc.select(ALL_ROWS_SELECTOR)
                .stream()
                .limit(1)//19
//                .skip(1)   //18
                .map(this::processOne)
                .collect(toCollection(LinkedList::new));
        scrapedUnitRepo.saveAll(list);
    }

    private ScrapedUnit processOne(Element elem) {
        ScrapedUnit scrapedUnit = new ScrapedUnit();
        scrapedUnit.setCompanyName(getColumnValue(elem, COMPANY_SELECTOR));
        scrapedUnit.setCountry(getColumnValue(elem, COUNTRY_SELECTOR));

        String pdfHref = elem.select(PDF_SELECTOR).attr("abs:href");
        if (pdfHref.isEmpty()) return scrapedUnit;
//        scrapedUnit.setSource(pdfHref);
        scrapedUnit.setSource("https://imocert.bio/wp-content/uploads/2021/01/20-UE-Certificado-Frhomimex.pdf");
        imoControlLationamericaLtdaParser.parse(scrapedUnit);
        return scrapedUnit;
    }


    private String getColumnValue(Element elem, String selector) {
        String val = elem.select(selector).get(0).text();
        return Objects.isNull(val) ? "" : val;
    }

}

package com.dataox.tradin_organic.service.scraper;

import com.dataox.tradin_organic.dao.entity.ScrapedUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import technology.tabula.Table;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@AllArgsConstructor
public class ControlunionParser {

    private final PdfConnection pdfConnection;

    public List<ScrapedUnit> parse(String url) {
        List<Table> tables = pdfConnection.extractRawText(Paths.get("src/main/resources/pdfTable.pdf"));
        for (int i = 0; i < 7; i++) {
            tables.get(3).getRows().remove(0);
        }
        if (CollectionUtils.isEmpty(tables)) return null;
        return tables.stream()
                .skip(2)
                .flatMap(table -> getInfoFromTable(table).stream())
                .collect(Collectors.toList());
    }

    private List<ScrapedUnit> getInfoFromTable(Table table) {
        return table.getRows().stream()
                .skip(8)
                .map(row -> {
                    ScrapedUnit scrapedUnit = new ScrapedUnit();
                    scrapedUnit.setCompanyName(row.get(2).getText());
                    scrapedUnit.setCountry(row.get(3).getText());
                    scrapedUnit.setAddress(row.get(4).getText());
                    scrapedUnit.setTelephone(row.get(5).getText());
                    scrapedUnit.setCertifiedProducts(row.get(6).getText());
                    scrapedUnit.setCertifiedActivities(row.get(8).getText().equals("1") ? "Primary " : null);
                    String primary = row.get(8).getText().equals("1") ? "Primary " : "";
                    String processing = row.get(9).getText().equals("1") ? "Processing" : "";
                    scrapedUnit.setCertifiedActivities(primary + " " + processing);
                    return scrapedUnit;
                })
                .collect(Collectors.toList());


    }
}

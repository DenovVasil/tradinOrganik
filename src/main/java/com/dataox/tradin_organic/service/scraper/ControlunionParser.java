package com.dataox.tradin_organic.service.scraper;

import com.dataox.tradin_organic.dao.entity.ScrapedUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@AllArgsConstructor
public class ControlunionParser {

    private final PdfConnection pdfConnection;

    public List<ScrapedUnit> parse(String url) {
        List<Table> tables = pdfConnection.extractRawText(Paths.get("src/main/resources/pdfTable.pdf"));
        tables.get(2).getRows().subList(0, 8).clear();
        if (tables.isEmpty()) {
            return Collections.emptyList();
        }
        return tables.stream()
                .skip(2)
                .map(this::getInfoFromTable)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<ScrapedUnit> getInfoFromTable(Table table) {
        return table.getRows().stream()
                .map(this::parseOneRow)
                .collect(Collectors.toList());
    }

    private ScrapedUnit parseOneRow(List<RectangularTextContainer> row) {
        ScrapedUnit scrapedUnit = new ScrapedUnit();
        scrapedUnit.setCompanyName(row.get(2).getText());
        scrapedUnit.setCountry(row.get(3).getText());
        scrapedUnit.setAddress(row.get(4).getText());
        scrapedUnit.setTelephone(row.get(5).getText());
        scrapedUnit.setCertifiedProducts(row.get(6).getText());
        String primary = fetchPrimary(row.get(8));
        String processing = row.get(9).getText().equals("1") ? "Processing" : "";
        scrapedUnit.setCertifiedActivities(primary + " " + processing);
        return scrapedUnit;
    }

    private String fetchPrimary(RectangularTextContainer container) {
        return container.getText().equals("1") ? "Primary " : "";
    }
}

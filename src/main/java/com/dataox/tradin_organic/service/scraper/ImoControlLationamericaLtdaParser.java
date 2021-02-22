package com.dataox.tradin_organic.service.scraper;

import com.dataox.tradin_organic.dao.entity.ScrapedUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import technology.tabula.Table;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

@Service
@Slf4j
@AllArgsConstructor
public class ImoControlLationamericaLtdaParser {

    private static final Pattern PATTERN_1 = Pattern.compile("(Ámbito\\(s\\) de actividad)((\\n.*)*)(Ver lista de)");
    private static final Pattern PATTERN_2 = Pattern.compile("(mbito\\(s\\) de actividad)((.*\\n.*\\n.*)((Ecológico\\s?)\\/ Organic))");
    private static final Pattern PRODUCT_VALUE = Pattern.compile("^[ ]{0,30}([\\p{L}()]{2,18}[ ]{0,2}){1,4}[ ]{2,4}");
    private final PdfConnection pdfConnection;


    public ScrapedUnit parse(ScrapedUnit scrapedUnit) {

        String text = pdfConnection.getPDFBoxText(scrapedUnit.getSource(), 1, 1);  // ternar operator

        if (isItPhoto(text) || Objects.isNull(text)) return scrapedUnit;
        text = fixTextData(text);
        // проверка на фото для продуктов по первой стр
        String url = scrapedUnit.getSource();
        scrapedUnit.setCertifiedActivities(parseActivityData(text));
        scrapedUnit.setCertifiedProducts(parseProductData(url));

        return scrapedUnit;
    }

    private String parseActivityData(String text) {

        // вызов текста
        Matcher m1 = PATTERN_1.matcher(text);
        Matcher m2 = PATTERN_2.matcher(text);
        String activityData = "";
        if (m1.find()) activityData = StringUtils.normalizeSpace(m1.group(2));

        if ((activityData) != "") {
            return splitData(activityData, "(?i)productos.*\\n.*products").trim();
        }

        if (m2.find()) activityData = m2.group(3);

        if (activityData != "") {
            activityData = StringUtils.remove(activityData, "Categoría(s) / Categary(ies)");
            activityData = splitData(activityData, "((?i)Ecológico.*conversion)");
            activityData = StringUtils.removeStart(activityData, "/");
            return activityData;
        }
        return null;
    }

    private String parseProductData(String url) {
        try {
            List<Table> tables = pdfConnection.extractPdfTableOld(url, 2);
            if (CollectionUtils.isEmpty(tables)) return null;
            return tables.stream()
                    .map(this::getDataFromTable)
                    .filter(Objects::nonNull)
                    .collect(joining("\n"));
        }catch (Exception e){
            log.error("error fetching url {}", url);
            return null;
        }
    }

    private String getDataFromTable(Table table) {
        int columnCount = table.getColCount();
        if (columnCount >= 3 && table.getRows().get(0).get(1).getText().isEmpty()) {
            return table.getRows().stream()
                    .skip(2)
                    .map(row -> fixProductData(row.get(0).getText()))
                    .collect(joining("\n"));
        }
        if (columnCount >= 3 && !table.getRows().get(0).get(1).isEmpty()) {
            return table.getRows().stream()
                    .map(row -> fixProductData(row.get(0).getText()))
                    .collect(joining("\n"));
        }
        return null;
    }


    private String splitData(String text, String splitter) {
        String data = Stream.of(text.split(splitter))
                .map(String::trim)
                .collect(joining(" "));
        return data.replaceAll("\n", " ");
    }


    private String fixTextData(String text) {
//        RegExUtils.removeAll("hello", "l");
        text = text.replaceAll("\\x00", "ti");
        text = text.replaceAll(String.valueOf((char) 160), " ");
        return text.replaceAll("ActMty\\{lesJ scape", "Activity(ies) scope");
    }

    private String fixProductData(String str) {
        return StringUtils.normalizeSpace(str).trim();
    }

    private boolean isItPhoto(String text) {
        return ("Copia del Original\n").equals(text);
    }

}

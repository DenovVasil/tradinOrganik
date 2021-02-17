package com.dataox.tradin_organic.service.scraper;

import com.dataox.tradin_organic.dao.entity.ScrapedUnit;
import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.exporting.PdfImageInfo;
import io.github.jonathanlink.PDFLayoutTextStripper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;


import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.io.TikaInputStream;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.awt.SystemColor.text;


@Service
@Slf4j
@AllArgsConstructor
public class ImoControlLationamericaLtdaParser {

    private static final Pattern pattern1 = Pattern.compile("(Ámbito\\(s\\) de actividad)((\\n.*)*)(Ver lista de)");
    private static final Pattern pattern2 = Pattern.compile("(mbito\\(s\\) de actividad)((.*\\n.*\\n.*)((Ecológico\\s?)\\/ Organic))");
    private final PdfConnection pdfConnection;


    public ScrapedUnit parse(ScrapedUnit scrapedUnit) {

        String text = pdfConnection.getPDFBoxText(scrapedUnit.getSource(), 1, 1);  // ternar operator
        if (isItPhoto(text) || Objects.isNull(text)) return scrapedUnit;
        text = fixTextData(text);
        scrapedUnit.setCertifiedActivities(parseActivityData(text));
        return scrapedUnit;
    }

    private String parseActivityData(String text) {
        Matcher m1 = pattern1.matcher(text);
        Matcher m2 = pattern2.matcher(text);
        String activityData = "";
        if (m1.find()) activityData = m1.group(2);

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

    private String splitData(String text, String splitter) {
        String data = Stream.of(text.split(splitter))
                .map(String::trim)
                .collect(Collectors.joining(" "));
        return data.replaceAll("\n", " ");
    }


    private String fixTextData(String text) {
        text = text.replaceAll("\\x00", "ti");
        return text.replaceAll("ActMty\\{lesJ scape", "Activity(ies) scope");
    }

    private boolean isItPhoto(String text) {
        return ("Copia del Original\n").equals(text);
    }

}

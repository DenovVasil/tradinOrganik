package com.dataox.tradin_organic.service.scraper;

import com.dataox.tradin_organic.dao.entity.ScrapedUnit;
import io.github.jonathanlink.PDFLayoutTextStripper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.springframework.stereotype.Service;
import technology.tabula.*;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class PdfConnection {


    public String getPDFPayloadText(String pdfUrl, int fPage, int lPage) {
        if (Objects.isNull(getPdfUrl(pdfUrl))) return null;
        try (PDDocument document = PDDocument.load(TikaInputStream.get(getPdfUrl(pdfUrl)))) {
            PDFTextStripper reader = new PDFLayoutTextStripper();
            reader.setStartPage(fPage);
            reader.setEndPage(lPage);
            return reader.getText(document);
        } catch (IOException ex) {
            log.error("Can not get text from url {} reason {}", pdfUrl, ex.getMessage());
            return null;
        }
    }


    public String getPDFPayloadText(String pdfUrl, int fPage) {
        log.info("Fetching url {}", pdfUrl);
        if (Objects.isNull(getPdfUrl(pdfUrl))) return null;
        try {
            PDDocument document = PDDocument.load(TikaInputStream.get(getPdfUrl(pdfUrl)));
            PDFTextStripper reader = new PDFLayoutTextStripper();
            reader.setStartPage(fPage);
            reader.setEndPage(document.getNumberOfPages());
            return reader.getText(document);
        } catch (IndexOutOfBoundsException ex) {
            log.error("FFFFacing link {}", pdfUrl);
            return null;
        } catch (IOException ex) {
            log.error("Can not get text from url {} reason {}", pdfUrl, ex.getMessage());
            return null;
        }
    }

//    public String getPDFPayloadText(String pdfUrl, int fPage) {
//        if (Objects.isNull(getPdfUrl(pdfUrl))) return null;
//        StringJoiner joiner = new StringJoiner("\n");
//        int pagesCount = fPage;
//        for (int i = fPage; i <= pagesCount + 1; i++) {
//            try (PDDocument document = PDDocument.load(TikaInputStream.get(getPdfUrl(pdfUrl)))) {
//                PDFTextStripper reader = new PDFLayoutTextStripper();
//                reader.setStartPage(i);
//                reader.setEndPage(i);
//                pagesCount = document.getNumberOfPages() - 1;
//                joiner.add(reader.getText(document));
//                //result = result + reader.getText(document);
//            } catch (IOException ex) {
//                log.error("Can not get text from url {} reason {}", pdfUrl, ex.getMessage());
//                return null;
//            } catch (IndexOutOfBoundsException ex) {
//                log.error("FFFacing  url  {}", pdfUrl);
//            }
//        }
//        return joiner.toString();
//    }

    public String getPDFBoxText(String pdfUrl, int fPage, int lPage) {
        if (Objects.isNull(getPdfUrl(pdfUrl))) return null;
        try (PDDocument document = PDDocument.load(TikaInputStream.get(getPdfUrl(pdfUrl)))) {
            PDFTextStripper reader = new PDFTextStripper();
            reader.setStartPage(fPage);
            reader.setEndPage(lPage);
            return reader.getText(document);
        } catch (IOException ex) {
            log.error("Can not get text from url {} reason {}", pdfUrl, ex.getMessage());
            return null;
        }

    }

    public String getPDFBoxText(String pdfUrl, int fPage) {
        // url =getPdfUrl
        if (Objects.isNull(getPdfUrl(pdfUrl))) return null;
        try (PDDocument document = PDDocument.load(TikaInputStream.get(getPdfUrl(pdfUrl)))) {
            PDFTextStripper reader = new PDFTextStripper();
            reader.setStartPage(fPage);
            reader.setEndPage(document.getNumberOfPages());
            return reader.getText(document).replaceAll("\\x00", "ti");
        } catch (IOException ex) {
            log.info("Can not get text from url {} reason {}", pdfUrl, ex.getMessage());
            return null;
        }
    }


    private URL getPdfUrl(String pdfUrl) {
        try {
            URL url = new URL(pdfUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() != 404) return url;
        } catch (IOException e) {
            log.error("Problem to get info from url {} , reason {}", pdfUrl, e.getMessage());
        }
        return null;
    }


    public List<Table> extractPdfTable(String url) throws IOException {
        try (PDDocument document = PDDocument.load(TikaInputStream.get(new URL(url)))) {
            SpreadsheetExtractionAlgorithm algorithm = new SpreadsheetExtractionAlgorithm();
            ObjectExtractor extractor = new ObjectExtractor(document);
            return IteratorUtils.toList(extractor.extract())
                    .stream()
                    .map(algorithm::extract)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
    }

    public List<Table> extractPdfTableOld(String url, int startedPage) {
        log.info("Fetching url{}", url);
        List<Table> tables = new ArrayList<>();
        Set<Table> temporary = new LinkedHashSet<>();

        try (PDDocument document = PDDocument.load(TikaInputStream.get(new URL(url)))) {
            ObjectExtractor oe = new ObjectExtractor(document);
            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
            if (document.getNumberOfPages() == 1) return tables;
            for (int i = startedPage; i <= document.getNumberOfPages(); i++) {
                Page page = oe.extract(i);
                temporary.addAll((List<Table>) sea.extract(page));
            }
        } catch (IOException e) {
            log.error("Problem to get info from url {} , reason {}", url, e.getMessage());
            return null;
        } catch (IndexOutOfBoundsException e) {
            log.error("In url {} page number {} does not exist", url);
        }
        tables.addAll(temporary);
        return tables;
    }

    public List<Table> extractRawText(Path pdfPath) {
        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            SpreadsheetExtractionAlgorithm algorithm = new SpreadsheetExtractionAlgorithm();
            ObjectExtractor extractor = new ObjectExtractor(document);
            List<Table> tables = IteratorUtils.toList(extractor.extract())
                    .stream()
                    .map(algorithm::extract)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

        return tables;
        } catch(IOException ex){
            log.error("Problem to get info from url");
            return null;
        }

    }

}
package com.dataox.tradin_organic.service.scraper;

import io.github.jonathanlink.PDFLayoutTextStripper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

@Service
@Slf4j
@AllArgsConstructor
public class PdfConnection {
//            text = text.replaceAll("\\x00", "ti");
//            return text.replaceAll("ActMty\\{lesJ scape", "Activity(ies) scope");


    public String getPDFPayloadText(String pdfUrl, int fPage, int lPage) {
        if (Objects.isNull(getPdfUrl(pdfUrl))) return null;
        try(PDDocument document = PDDocument.load(TikaInputStream.get(getPdfUrl(pdfUrl)))) {
            PDFTextStripper reader = new PDFLayoutTextStripper();
            reader.setStartPage(fPage);
            reader.setEndPage(lPage);
            return reader.getText(document);
        }catch(IOException ex){
            log.error("Can not get text from url {} reason {}", pdfUrl, ex.getMessage());
            return null;
        }
    }


    public String getPDFPayloadText(String pdfUrl, int fPage) {
        if (Objects.isNull(getPdfUrl(pdfUrl))) return null;
        try(PDDocument document = PDDocument.load(TikaInputStream.get(getPdfUrl(pdfUrl)))) {
            PDFTextStripper reader = new PDFLayoutTextStripper();
            reader.setStartPage(fPage);
            reader.setEndPage(document.getNumberOfPages());
            return reader.getText(document);
        }catch(IOException ex){
            log.error("Can not get text from url {} reason {}", pdfUrl, ex.getMessage());
            return null;
        }
    }


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
        if (Objects.isNull(getPdfUrl(pdfUrl))) return null;
        try (PDDocument document = PDDocument.load(TikaInputStream.get(getPdfUrl(pdfUrl)))) {
            PDFTextStripper reader = new PDFTextStripper();
            reader.setStartPage(fPage);
            reader.setEndPage(document.getNumberOfPages());
            return reader.getText(document).replaceAll("\\x00", "ti");
        }catch(IOException ex){
            log.error("Can not get text from url {} reason {}", pdfUrl, ex.getMessage());
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

}

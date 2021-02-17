package com.dataox.tradin_organic.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class JsoupConnection {

    public Document getDocument(String url){
        log.info("Fetching {}", url);
        try {
            return Jsoup.connect(url).get();
        }catch (IOException e){
            log.error("Error getting {} reason{}", url, e.getMessage());
            return null;
        }
    }
}

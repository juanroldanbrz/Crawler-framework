package com.yamajun.crawler.experimental

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonSlurper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate

class GnulaScript implements CrawlerInterface {

    boolean decideIfCrawl(Document document) {
        return document.getElementsByClass('poster').size() == 1;
    }

    Map<String, Object> extractData(Document document) {
        String pictureUrl = document.select('#main-poster')?.attr('abs:src');
        String name = document.select('#media-profile-backdrop > div.media-backdrop-text > h1 > span').text();
        String description = document.select('#media-plot').text();
        String originalTitle = document.select('#media-info > div.additional-info > div > span').text();
        List<String> links = getLinks(document);
        return ['pictureUrl' : pictureUrl, 'movieName' : name, 'movieDescription' : description, 'originalTitle' : originalTitle, links: links];
    };

    def getLinks(Document document){
        def headersMap = ['Accept':'text/*',
                          'Accept-Language':'es-ES,es;q=0.9,en;q=0.8',
                          'Cache-Control':'no-cache',
                          'Connection':'keep-alive',
                          'Host':'gnula.biz',
                          'Pragma':'no-cache',
                          'Referer':document.location(),
                          'User-Agent':'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36',
                          'X-Requested-With':'XMLHttpRequest'];
        String idm = document.select('#media-profile').attr('data-idm');
        String mediaType = document.select('#media-profile').attr('data-media-type');
        String url = 'http://gnula.biz/media/partials?idm=' + idm + '&mediaType=' + mediaType;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders(headersMap);
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        def result = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        def objectToCrawl = new JsonSlurper().parseText(result.getBody());
        String serializedHtml = JsonPath.read(objectToCrawl, "\$.sources");
        def linkDocument = Jsoup.parse(serializedHtml);
        return linkDocument.getElementsByClass("available-source").findAll({it.attr('data-url')}).collect({ it.attr("data-url")});
    }
}

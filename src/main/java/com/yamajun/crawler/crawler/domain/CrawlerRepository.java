package com.yamajun.crawler.crawler.domain;

import com.yamajun.crawler.crawler.domain.model.Crawler;
import com.yamajun.crawler.crawler.domain.model.Extraction;
import com.yamajun.crawler.crawler.domain.model.UrlData;
import java.util.List;

public interface CrawlerRepository {

  UrlData addUrlData(UrlData urlData);

  UrlData updateUrlData(UrlData urlData);

  List<UrlData> findNotVisitedPages(int numberOfPages);

  Crawler findCrawlerByName(String name);

  Crawler addCrawler(Crawler crawler);

  UrlData findUrlDataById(String id);

  Crawler updateCrawler(Crawler crawler);

  void addExtraction(String crawlerId, Extraction extraction);

}

package com.yamajun.crawler.crawler.domain;

import com.yamajun.crawler.crawler.CrawlerConfig;
import com.yamajun.crawler.crawler.domain.model.CrawlerDomain;
import com.yamajun.crawler.crawler.domain.model.Extraction;
import com.yamajun.crawler.crawler.domain.model.UrlData;
import java.util.List;

public interface CrawlerRepository {

  UrlData addUrlData(UrlData urlData);

  UrlData updateUrlData(UrlData urlData);

  List<UrlData> findNotVisitedPages(int numberOfPages);

  CrawlerDomain findCrawlerByName(String name);

  CrawlerDomain addCrawler(CrawlerDomain crawlerDomain);

  UrlData findUrlDataById(String id);

  CrawlerDomain updateCrawler(CrawlerDomain crawlerDomain);

  List<CrawlerDomain> findAllCrawler();

  void addExtraction(String crawlerId, Extraction extraction);

  void updateConfiguration(String crawlerId, CrawlerConfig crawlerConfig);
}

package com.yamajun.crawler.process;

import com.yamajun.crawler.model.Crawler;
import com.yamajun.crawler.repository.CrawlerProcessRepository;
import com.yamajun.crawler.repository.UrlDataRepository;
import com.yamajun.crawler.repository.UrlExtractionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CrawlerProcessFactory {

  private final UrlDataRepository urlDataRepository;
  private final UrlExtractionRepository urlExtractionRepository;
  private final CrawlerProcessRepository crawlerProcessRepository;

  @Autowired
  public CrawlerProcessFactory(UrlDataRepository urlDataRepository,
      UrlExtractionRepository urlExtractionRepository,
      CrawlerProcessRepository crawlerProcessRepository) {
    this.urlDataRepository = urlDataRepository;
    this.urlExtractionRepository = urlExtractionRepository;
    this.crawlerProcessRepository = crawlerProcessRepository;
  }

  public CrawlerProcess createProcess(Crawler crawler) {
    return new CrawlerProcess(crawler, urlDataRepository, urlExtractionRepository, crawlerProcessRepository);
  }

}

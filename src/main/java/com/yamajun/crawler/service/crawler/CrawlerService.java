package com.yamajun.crawler.service.crawler;

import com.yamajun.crawler.model.CrawlerConfig;
import com.yamajun.crawler.model.Crawler;
import java.util.List;

public interface CrawlerService {

  /** Save and start crawler **/
  Crawler addAndStart(Crawler crawler);

  void removeAndStop(String crawlerId);

  void startCrawler(String crawlerId);

  void stopCrawler(String crawlerId);

  Crawler findById(String crawlerId);

  List<Crawler> findAll();

  void updateConfig(String crawlerId, CrawlerConfig newConfig);

}

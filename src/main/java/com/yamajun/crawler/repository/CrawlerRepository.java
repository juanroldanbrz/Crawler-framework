package com.yamajun.crawler.repository;

import com.yamajun.crawler.model.Crawler;
import com.yamajun.crawler.model.CrawlerConfig;
import java.util.List;

public interface CrawlerRepository {

  List<Crawler> findAll();

  Crawler findById(String id);

  Crawler save(Crawler crawler);

  void remove(String crawlerId);

  void updateConfig(String crawlerId, CrawlerConfig crawlerConfig);
}

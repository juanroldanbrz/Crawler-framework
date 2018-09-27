package com.yamajun.crawler.manager;

import com.yamajun.crawler.crawler.CrawlerConfig;
import com.yamajun.crawler.experimental.CrawlerData;
import com.yamajun.crawler.experimental.CrawlerStats;
import java.util.List;
import java.util.Map;

public interface CrawlerService {

  CrawlerData registerCrawler(CrawlerConfig crawlerConfig);

  void unregisterCrawler(String crawlerId);

  void startCrawler(String crawlerId);

  CrawlerStats stopCrawler(String crawlerId);

  CrawlerData getData(String crawlerId, CrawlerData.Type[] contains);

  List<CrawlerData> getAllData(CrawlerData.Type[] contains);

  CrawlerConfig updateConfig(String crawlerId, CrawlerConfig newConfig);

}

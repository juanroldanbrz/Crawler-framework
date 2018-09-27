package com.yamajun.crawler.manager;

import com.yamajun.crawler.crawler.CrawlerConfig;
import com.yamajun.crawler.crawler.CrawlerProcess;
import com.yamajun.crawler.experimental.CrawlerData;
import com.yamajun.crawler.experimental.CrawlerData.Type;
import com.yamajun.crawler.experimental.CrawlerStats;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CrawlerServiceImpl implements CrawlerService {

  private final CrawlerProcessContainer container;

  @Autowired
  public CrawlerServiceImpl(final CrawlerProcessContainer container) {
    this.container = container;
  }

  @Override
  public CrawlerData registerCrawler(CrawlerConfig crawlerConfig) {
    var crawlerProcess = new CrawlerProcess(crawlerConfig);
    crawlerProcess.initializeCrawler();
    return container.loadProcess(crawlerProcess);
  }

  @Override
  public void unregisterCrawler(String crawlerId) {
    container.unloadProcess(crawlerId);
  }

  @Override
  public void startCrawler(String crawlerId) {
    container.startProcess(crawlerId);
  }

  @Override
  public CrawlerStats stopCrawler(String crawlerId) {
    return container.stopProcess(crawlerId);
  }

  @Override
  public CrawlerData getData(String crawlerId, Type[] contains) {
    return container.getData(crawlerId, contains);
  }

  @Override
  public List<CrawlerData> getAllData(Type[] contains) {
    return container.getAllData(contains);
  }

  @Override
  public CrawlerConfig updateConfig(String crawlerId, CrawlerConfig newConfig) {
    container.getProcess(crawlerId).updateConfig(newConfig);
    return newConfig;
  }
}

package com.yamajun.crawler.service.crawler;

import com.yamajun.crawler.model.Crawler;
import com.yamajun.crawler.model.CrawlerConfig;
import com.yamajun.crawler.process.CrawlerProcessFactory;
import com.yamajun.crawler.repository.CrawlerRepository;
import com.yamajun.crawler.service.process.CrawlerProcessContainer;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CrawlerServiceImpl implements CrawlerService {

  private final CrawlerProcessContainer container;
  private final CrawlerRepository crawlerRepository;
  private final CrawlerProcessFactory crawlerProcessFactory;

  @Autowired
  public CrawlerServiceImpl(final CrawlerProcessContainer container, final CrawlerRepository crawlerRepository,
      CrawlerProcessFactory crawlerProcessFactory) {
    this.container = container;
    this.crawlerRepository = crawlerRepository;
    this.crawlerProcessFactory = crawlerProcessFactory;
  }

  @Override
  public Crawler addAndStart(Crawler crawler) {
    crawlerRepository.save(crawler);
    var crawlerProcess = crawlerProcessFactory.createProcess(crawler);
    container.loadAndStartProcess(crawlerProcess);
    return crawler;
  }

  @Override
  public void removeAndStop(String crawlerId) {
    crawlerRepository.remove(crawlerId);
    container.unloadAndStopProcess(crawlerId);
  }

  @Override
  public void startCrawler(String crawlerId) {
    container.startProcess(crawlerId);
  }

  @Override
  public void stopCrawler(String crawlerId) {
    container.stopProcess(crawlerId);
  }

  @Override
  public Crawler findById(String crawlerId) {
    return crawlerRepository.findById(crawlerId);
  }

  @Override
  public List<Crawler> findAll() {
    return crawlerRepository.findAll();
  }

  @Override
  public void updateConfig(String crawlerId, CrawlerConfig newConfig) {
    crawlerRepository.updateConfig(crawlerId, newConfig);
  }
}

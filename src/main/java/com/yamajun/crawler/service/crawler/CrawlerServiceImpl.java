package com.yamajun.crawler.service.crawler;

import com.yamajun.crawler.experimental.GroovyScript.Type;
import com.yamajun.crawler.model.Crawler;
import com.yamajun.crawler.model.CrawlerConfig;
import com.yamajun.crawler.process.CrawlerProcessFactory;
import com.yamajun.crawler.repository.CrawlerRepository;
import com.yamajun.crawler.service.process.CrawlerProcessContainer;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CrawlerServiceImpl implements CrawlerService, ApplicationListener<ContextRefreshedEvent> {

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

  @Override
  public void modifyScript(String crawlerId, Type type, String content) {

  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    var crawlers = crawlerRepository.findAll();
    for(var crawler : crawlers){
      var crawlerProcess = crawlerProcessFactory.createProcess(crawler);
      log.info("Loading crawler. <_id:{}>", crawler.get_id());
      container.loadAndStartProcess(crawlerProcess);
      log.info("Loaded crawler. <_id:{}>", crawler.get_id());
    }
  }
}

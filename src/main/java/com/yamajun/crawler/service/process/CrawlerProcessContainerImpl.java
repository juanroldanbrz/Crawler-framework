package com.yamajun.crawler.service.process;

import com.yamajun.crawler.process.CrawlerProcess;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CrawlerProcessContainerImpl implements CrawlerProcessContainer {

  private final Map<String, CrawlerProcess> processMap;

  public CrawlerProcessContainerImpl() {
    processMap = new ConcurrentHashMap<>();
  }

  @Override
  public void loadAndStartProcess(CrawlerProcess crawlerProcess) {
    var crawlerId = crawlerProcess.getCrawlerId();

    if (processMap.containsKey(crawlerId)) {
      throw new RuntimeException("Cannot load a process which is already loaded");
    }

    processMap.put(crawlerId, crawlerProcess);
    crawlerProcess.init();
    startProcess(crawlerId);
  }

  @Override
  public void unloadAndStopProcess(String crawlerId) {
    getOrFail(crawlerId).stopCrawler();
    processMap.remove(crawlerId);
  }

  @Override
  public void startProcess(String crawlerId) {
    getOrFail(crawlerId).startCrawler();
  }

  @Override
  public void stopProcess(String crawlerId) {
    getOrFail(crawlerId).stopCrawler();
  }

  private CrawlerProcess getOrFail(String crawlerId) {
    var process = processMap.get(crawlerId);
    if (process == null) {
      throw new RuntimeException("Process is not in the map. Id : " + crawlerId);
    }
    return process;
  }
}

package com.yamajun.crawler.manager;

import com.yamajun.crawler.crawler.CrawlerProcess;
import com.yamajun.crawler.crawler.domain.model.CrawlerDomain;
import com.yamajun.crawler.experimental.CrawlerData;
import com.yamajun.crawler.experimental.CrawlerData.Type;
import com.yamajun.crawler.experimental.CrawlerStats;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CrawlerProcessContainerImpl implements CrawlerProcessContainer {

  public final Map<String, CrawlerProcess> workingCrawlers;

  public CrawlerProcessContainerImpl() {
    workingCrawlers = new ConcurrentHashMap<>();
  }

  @Override
  public List<CrawlerData> getAllData(Type[] contains) {
    return workingCrawlers.entrySet().stream()
        .map(Entry::getKey)
        .map(this::getOrFault)
        .map(this::toCrawlerData)
        .collect(Collectors.toList());
  }

  @Override
  public CrawlerData getData(String crawlerId, Type[] contains) {
    return toCrawlerData(getOrFault(crawlerId));
  }

  @Override
  public CrawlerData loadProcess(CrawlerProcess crawlerProcess) {
    workingCrawlers.put(crawlerProcess.getCrawlerId(), crawlerProcess);
    return startProcess(crawlerProcess.getCrawlerId());
  }

  @Override
  public void unloadProcess(String crawlerId) {
    stopProcess(crawlerId);
    workingCrawlers.remove(crawlerId);
  }

  @Override
  public CrawlerData startProcess(String crawlerId) {
    var crawler = getOrFault(crawlerId);
    crawler.startCrawler();
    return toCrawlerData(crawler);
  }

  @Override
  public CrawlerStats stopProcess(String crawlerId) {
    var crawler = getOrFault(crawlerId);
    crawler.stopCrawler();
    return toCrawlerData(crawler).getStats();
  }

  @Override
  public CrawlerProcess getProcess(String crawlerId) {
    return getOrFault(crawlerId);
  }

  private CrawlerProcess getOrFault(String crawlerId){
    if(workingCrawlers.containsKey(crawlerId)){
      return workingCrawlers.get(crawlerId);
    } else {
      throw new RuntimeException("Change this please");
    }
  }

  public CrawlerData toCrawlerData(CrawlerProcess crawler){
    return fromId(crawler.getCrawlerId())
        .append(
            new CrawlerStats(crawler.getPagesCrawled(), crawler.getNumOfExtractions(), crawler.getNumOfExecutions()))
        .append(crawler.getCrawlerConfig());
  }

  public CrawlerData fromId(String id){
    CrawlerData crawlerData = new CrawlerData();
    crawlerData.setId(id);
    return crawlerData;
  }
}

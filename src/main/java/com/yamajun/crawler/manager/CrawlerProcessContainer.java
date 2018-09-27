package com.yamajun.crawler.manager;

import com.yamajun.crawler.crawler.CrawlerProcess;
import com.yamajun.crawler.crawler.domain.model.CrawlerDomain;
import com.yamajun.crawler.experimental.CrawlerData;
import com.yamajun.crawler.experimental.CrawlerStats;
import java.util.List;
import java.util.Map;

public interface CrawlerProcessContainer {

  List<CrawlerData> getAllData(CrawlerData.Type[] contains);

  CrawlerData getData(String crawlerId, CrawlerData.Type[] contains);

  CrawlerData loadProcess(CrawlerProcess crawlerProcess);

  void unloadProcess(String crawlerId);

  CrawlerData startProcess(String crawlerId);

  CrawlerStats stopProcess(String crawlerId);

  CrawlerProcess getProcess(String crawlerId);

}

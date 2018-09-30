package com.yamajun.crawler.service.process;

import com.yamajun.crawler.process.CrawlerProcess;

public interface CrawlerProcessContainer {

  void loadAndStartProcess(CrawlerProcess crawlerProcess);

  void unloadAndStopProcess(String crawlerId);

  void startProcess(String crawlerId);

  void stopProcess(String crawlerId);

}

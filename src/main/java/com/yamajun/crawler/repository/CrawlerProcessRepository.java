package com.yamajun.crawler.repository;

import com.yamajun.crawler.model.status.CrawlerStatus;
import com.yamajun.crawler.process.model.SynchronizedCrawlerStats;

public interface CrawlerProcessRepository {

  void updateStats(String crawlerId, SynchronizedCrawlerStats stats);

  void updateCrawlerStatus(String crawlerId, CrawlerStatus status);

}

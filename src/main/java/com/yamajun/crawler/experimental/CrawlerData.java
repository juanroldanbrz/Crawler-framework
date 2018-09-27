package com.yamajun.crawler.experimental;

import com.yamajun.crawler.crawler.CrawlerConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrawlerData {

  private String id;

  private CrawlerConfig crawlerConfig;
  private CrawlerStats stats;

  public enum Type {
    CRAWLER_DEFINITION,
    EXTRACTION_RESULT,
    CRAWLER_CONFIG,
    STATS
  }

  public CrawlerData append(CrawlerConfig crawlerConfig){
    this.crawlerConfig = crawlerConfig;
    return this;
  }

  public CrawlerData append(CrawlerStats stats){
    this.stats = stats;
    return this;
  }
}

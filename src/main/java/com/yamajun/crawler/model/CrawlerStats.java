package com.yamajun.crawler.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrawlerStats {

  private long pagesCrawled = 0;
  private long numOfExtractions = 0;
  private long numOfExecutions = 0;

}

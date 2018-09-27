package com.yamajun.crawler.experimental;

import java.util.concurrent.atomic.AtomicLong;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CrawlerStats {

  private long pagesCrawled;
  private long numOfExtractions;
  private long numOfExecutions;

}

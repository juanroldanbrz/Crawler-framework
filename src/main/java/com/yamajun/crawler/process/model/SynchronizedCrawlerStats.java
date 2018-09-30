package com.yamajun.crawler.process.model;

import java.util.concurrent.atomic.AtomicLong;

public class SynchronizedCrawlerStats {

  private AtomicLong pagesCrawled = new AtomicLong();
  private AtomicLong numOfExtractions = new AtomicLong();
  private AtomicLong numOfExecutions = new AtomicLong();

  public long getPagesCrawled() {
    return pagesCrawled.get();
  }

  public void setPagesCrawled(long pagesCrawled) {
    this.pagesCrawled.set(pagesCrawled);
  }

  public long getNumOfExtractions() {
    return numOfExtractions.get();
  }

  public void setNumOfExtractions(long numOfExtractions) {
    this.numOfExtractions.set(numOfExtractions);
  }

  public long getNumOfExecutions() {
    return numOfExecutions.get();
  }

  public void setNumOfExecutions(long numOfExecutions) {
    this.numOfExecutions.set(numOfExecutions);
  }

  public void incPagesCrawled() {
    pagesCrawled.incrementAndGet();
  }

  public void incNumOfExtractions() {
    numOfExtractions.incrementAndGet();
  }

  public void incNumOfExecutions() {
    numOfExecutions.incrementAndGet();
  }
}

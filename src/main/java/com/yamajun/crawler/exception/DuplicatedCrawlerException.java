package com.yamajun.crawler.exception;

public class DuplicatedCrawlerException extends RuntimeException {

  public DuplicatedCrawlerException(String msg){
    super(msg);
  }

  public DuplicatedCrawlerException(String msg, Throwable t){
    super(msg, t);
  }

}

package com.yamajun.crawler.exception;

public class ConnectionException extends RuntimeException {

  public ConnectionException(String msg){
    super(msg);
  }

  public ConnectionException(String msg, Throwable t){
    super(msg, t);
  }

}

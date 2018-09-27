package com.yamajun.crawler.experimental;

public interface Process {

  void startProcess();

  void stopProcess();

  <T> T getSnapshot();

  String getProcessId();

}

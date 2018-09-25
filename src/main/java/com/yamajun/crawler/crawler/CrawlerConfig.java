package com.yamajun.crawler.crawler;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrawlerConfig {

  private String domainName;
  private String entryPointUrl;
  private int numOfThreads = 1;

  private List<String> whiteListContains;

}

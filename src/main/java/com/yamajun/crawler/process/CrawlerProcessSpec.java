package com.yamajun.crawler.process;

import com.yamajun.crawler.experimental.GroovyScript;
import com.yamajun.crawler.model.status.CrawlerStatus;
import java.util.List;
import java.util.Map;
import org.jsoup.nodes.Document;

public interface CrawlerProcessSpec {

  void init();

  void startCrawler();

  void stopCrawler();

  Document connect(String url);

  List<String> extractUrls(Document document);

  Map<String, Object> extractData(Document document, GroovyScript groovyScript);

  boolean canExtractData(Document document, GroovyScript groovyScript);
  /**
   * Main loop of the crawler
   **/
  void crawlerLoop();

  CrawlerStatus getStatus();

  String getCrawlerId();
}

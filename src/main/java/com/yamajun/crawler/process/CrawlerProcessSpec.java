package com.yamajun.crawler.process;

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

  Map<String, Object> extractData(Document document, Map<String, String> xpathRules);

  boolean canExtractData(Document document, String xpath);
  /**
   * Main loop of the crawler
   **/
  void crawlerLoop();

  CrawlerStatus getStatus();

  String getCrawlerId();
}

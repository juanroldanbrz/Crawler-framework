package com.yamajun.crawler.experimental;

import java.util.Map;
import org.jsoup.nodes.Document;

public interface CrawlerInterface {

  boolean decideIfCrawl(Document document);

  Map<String, Object> extractData(Document document);

}

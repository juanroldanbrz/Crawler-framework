package com.yamajun.crawler.test;

import com.yamajun.crawler.crawler.AbstractCrawler;
import com.yamajun.crawler.crawler.CrawlerConfig;
import com.yamajun.crawler.crawler.domain.model.Crawler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jongo.Jongo;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;


public class RepelisPlusCrawler extends AbstractCrawler {

  @Autowired
  public RepelisPlusCrawler(CrawlerConfig crawlerConfig, Jongo jongo) {
    super(crawlerConfig, jongo);
  }

  @Override
  public Map<String, String> extractData(Document document) {
    return null;
  }

  @Override
  public List<String> extractUrls(Document document) {
    var elements = document.select("a");
    var urlList = new ArrayList<String>();
    for(var element : elements){
      urlList.add(element.attr("abs:href"));
    }

    return urlList;
  }

  @Override
  public String getCrawlerName() {
    return "RepelisPlus";
  }

  @Override
  public boolean canExtractInfoFromThis(Document document) {
    return false;
  }
}

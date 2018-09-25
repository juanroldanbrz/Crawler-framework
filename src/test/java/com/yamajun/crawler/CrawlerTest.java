package com.yamajun.crawler;

import com.mongodb.MongoClient;
import com.yamajun.crawler.crawler.CrawlerConfig;
import com.yamajun.crawler.test.RepelisPlusCrawler;
import java.util.Collections;
import org.jongo.Jongo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CrawlerTest {

  @Autowired
  private Jongo jongo;

  @Autowired
  private MongoClient mongoClient;

  @Test
  public void test(){
    mongoClient.dropDatabase("crawler");
    CrawlerConfig crawlerConfig = new CrawlerConfig();
    crawlerConfig.setDomainName("repelisplus.ch");
    crawlerConfig.setNumOfThreads(10);
    crawlerConfig.setEntryPointUrl("https://www.repelisplus.ch/lanzamientos");
    crawlerConfig.setWhiteListContains(Collections.singletonList("www.repelisplus.ch"));
    RepelisPlusCrawler repelisPlusCrawler = new RepelisPlusCrawler(crawlerConfig, jongo);
    repelisPlusCrawler.startCrawler();
  }
}

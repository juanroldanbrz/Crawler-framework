package com.yamajun.crawler.controller;

import com.yamajun.crawler.model.Crawler;
import com.yamajun.crawler.service.crawler.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawler")
public class CrawlerController {

  @Autowired
  private CrawlerService crawlerManager;

  @PostMapping("")
  public Crawler createCrawler(@RequestBody Crawler crawlerDomain) {
    return crawlerManager.addAndStart(crawlerDomain);
  }
}

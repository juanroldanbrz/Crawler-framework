package com.yamajun.crawler.controller;

import com.yamajun.crawler.crawler.domain.model.CrawlerDomain;
import com.yamajun.crawler.manager.CrawlerService;
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
  public CrawlerDomain createCrawler(@RequestBody CrawlerDomain crawlerDomain){
    return crawlerManager.registerCrawler(crawlerDomain);
  }
}

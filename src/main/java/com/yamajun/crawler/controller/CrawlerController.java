package com.yamajun.crawler.controller;

import com.yamajun.crawler.experimental.GroovyScript.Type;
import com.yamajun.crawler.model.Crawler;
import com.yamajun.crawler.service.crawler.CrawlerService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawler")
public class CrawlerController {

  private final CrawlerService crawlerService;

  @Autowired
  public CrawlerController(CrawlerService crawlerService) {
    this.crawlerService = crawlerService;
  }

  @PostMapping("")
  public Crawler createCrawler(@RequestBody Crawler crawlerDomain) {
    return crawlerService.addAndStart(crawlerDomain);
  }

  @GetMapping("")
  public List<Crawler> listAll(){
    return crawlerService.findAll();
  }

  @PostMapping("/{id}/script/{type}")
  public void modifyScript(@PathVariable(name = "id") String crawlerId, @PathVariable Type type,
      @RequestBody String content){
    crawlerService.modifyScript(crawlerId, type, content);
  }
}

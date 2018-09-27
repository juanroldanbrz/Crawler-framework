package com.yamajun.crawler.crawler;

import static java.util.stream.Collectors.toList;

import com.yamajun.crawler.crawler.domain.CrawlerRepository;
import com.yamajun.crawler.crawler.domain.model.CrawlerDomain;
import com.yamajun.crawler.exception.ConnectionException;
import com.yamajun.crawler.crawler.domain.model.CrawlerStatus;
import com.yamajun.crawler.crawler.domain.model.Extraction;
import com.yamajun.crawler.crawler.domain.model.UrlData;
import com.yamajun.crawler.crawler.domain.model.UrlStatus;
import com.yamajun.crawler.xsoup.Xsoup;
import io.vavr.control.Try;
import java.io.IOException;
import java.io.ObjectInputFilter.Config;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class CrawlerProcess {

  private final ExecutorService executor;
  private final CrawlerConfig crawlerConfig;

  public CrawlerConfig getCrawlerConfig() {
    return crawlerConfig;
  }

  private final AtomicBoolean keepExecution;

  private String crawlerId;

  private AtomicLong pagesCrawled;
  private AtomicLong numOfExtractions;
  private AtomicLong numOfExecutions;

  public long getPagesCrawled() {
    return pagesCrawled.get();
  }

  public long getNumOfExtractions() {
    return numOfExtractions.get();
  }

  public long getNumOfExecutions() {
    return numOfExecutions.get();
  }

  @Autowired
  private CrawlerRepository crawlerRepository;

  public void updateConfig(CrawlerConfig crawlerConfig){
    crawlerRepository.updateConfiguration(crawlerId, crawlerConfig);
  }

  public CrawlerProcess(CrawlerConfig crawlerConfig) {
    this.crawlerConfig = crawlerConfig;
    executor = Executors.newFixedThreadPool(crawlerConfig.getNumOfThreads());
    keepExecution = new AtomicBoolean(true);
  }

  public final void startCrawler(){
    CrawlerDomain crawlerDomain = initializeCrawler();
    crawlerDomain.setNumOfExecutions(crawlerDomain.getNumOfExecutions() + 1);
    crawlerDomain.setLastExecutionStart(Instant.now());
    crawlerDomain.setStatus(CrawlerStatus.STARTED);
    crawlerRepository.updateCrawler(crawlerDomain);
    crawlerId = crawlerDomain.get_id();

    crawlLoop(crawlerDomain);
  }

  public final void stopCrawler(){
    keepExecution.set(false);
  }

  public boolean matchesWhiteList(String url){
    for(var keyword : crawlerConfig.getWhiteListContains()){
      if(url.contains(keyword)){
        return true;
      }
    }

    return false;
  }

  private void crawlLoop(final CrawlerDomain crawlerDomain){
    var linksToCrawlUrlData = crawlerRepository.findNotVisitedPages(crawlerConfig.getNumOfThreads());

    while (!linksToCrawlUrlData.isEmpty() && keepExecution.get()){
      var invokations = new ArrayList<Callable<Boolean>>();
      for(final var urlData : linksToCrawlUrlData){
        if(!keepExecution.get()){
          break;
        }
        var targetUrl = urlData.getUrl();
        log.info("Crawling {}", targetUrl);
        invokations.add(() -> {
          var document = connect(targetUrl);
          var urls = extractUrls(document);
          List<String> distinctUrls = urls.stream().distinct().map(this::normalizeUrl).filter(Objects::nonNull).collect(toList());
          for(var url : distinctUrls){
            if(matchesWhiteList(url)){
              UrlData newUrlData = new UrlData();
              newUrlData.setCrawlerParentId(crawlerDomain.get_id());
              newUrlData.setStatus(UrlStatus.NOT_CRAWLED);
              newUrlData.setUrl(url);
              crawlerRepository.addUrlData(newUrlData);
            }
          }
          var extractionDataMap = extractData(document);
          var extraction = new Extraction();
          extraction.setData(extractionDataMap);
          extraction.setUrl(targetUrl);
          crawlerRepository.addExtraction(crawlerDomain.get_id(), extraction);
          var urlDataToUpdate = crawlerRepository.findUrlDataById(urlData.get_id());
          urlDataToUpdate.setStatus(UrlStatus.CRAWLED);
          crawlerRepository.updateUrlData(urlDataToUpdate);
          return true;
        });
      }

      try {
        executor.invokeAll(invokations);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      linksToCrawlUrlData = crawlerRepository.findNotVisitedPages(crawlerConfig.getNumOfThreads());
    }
  }

  private String normalizeUrl(String url){
    var attempNormalize = Try.of(() -> new URL(url));
    if(attempNormalize.isFailure()){
      return null;
    } else {
      var urlWrap = attempNormalize.get();
      return urlWrap.getProtocol() + "://" + urlWrap.getHost() + urlWrap.getFile();
    }
  }


  public CrawlerDomain initializeCrawler(){
    var crawler = crawlerRepository.findCrawlerByName(crawlerConfig.getCrawlerName());
    if(crawler == null){
      var domain = crawlerConfig.getDomainName();

      crawler = new CrawlerDomain();
      crawlerConfig.setLastModification(Instant.now());
      crawler.setCrawlerConfig(crawlerConfig);
      crawler.setDomain(domain);
      crawler.setName(crawlerConfig.getCrawlerName());
      crawler.setNumOfExtractions(0);
      crawler.setPagesCrawled(0);
      crawler.setCreatedAt(Instant.now());
      crawler.setUpdatedAt(Instant.now());
      crawler.setNumOfExtractions(0);

      crawlerRepository.addCrawler(crawler);

      UrlData newUrlData = new UrlData();
      newUrlData.setCrawlerParentId(crawler.get_id());
      newUrlData.setStatus(UrlStatus.NOT_CRAWLED);
      newUrlData.setUrl(crawlerConfig.getEntryPointUrl());
      crawlerRepository.addUrlData(newUrlData);

    }

    this.pagesCrawled.set(crawler.getPagesCrawled());
    this.numOfExecutions.set(crawler.getNumOfExecutions());
    this.numOfExtractions.set(crawler.getNumOfExtractions());

    return crawler;
  }

  private Document connect(String url) {
    try {
      var toReturn = Jsoup.connect(url).get();
      this.pagesCrawled.incrementAndGet();
      return toReturn;
    } catch (IOException e) {
      throw new ConnectionException("Cannot connect to url: " + url, e);
    }
  }

  public Map<String, String> extractData(Document document){
    var operationConfig = crawlerConfig.getXpathExtractionRules();
    var extractionMap = new HashMap<String, String>();
    for(var operation : operationConfig.entrySet()){
      var resultKey = operation.getKey();
      var operationXpath = operation.getKey();
      extractionMap.put(resultKey, evaluateXpath(operationXpath, document));
    }

    return extractionMap;
  }

  private String evaluateXpath(String xpath, Document document){
    return Xsoup.compile(xpath).evaluate(document).get();
  }

  private List<String> extractUrls(Document document){
    var elements = document.select("a");
    var urlList = new ArrayList<String>();
    for(var element : elements){
      urlList.add(element.attr("abs:href"));
    }

    return urlList;
  }

  public String getCrawlerId(){
    return crawlerId;
  }

  private boolean canExtractData(Document document){
    return evaluateXpath(crawlerConfig.getMagicXpath(), document) != null;
  }
}

package com.yamajun.crawler.crawler;

import static java.util.stream.Collectors.toList;

import com.yamajun.crawler.crawler.domain.CrawlerRepository;
import com.yamajun.crawler.crawler.domain.CrawlerRepositoryImpl;
import com.yamajun.crawler.exception.ConnectionException;
import com.yamajun.crawler.crawler.domain.model.Crawler;
import com.yamajun.crawler.crawler.domain.model.CrawlerStatus;
import com.yamajun.crawler.crawler.domain.model.Extraction;
import com.yamajun.crawler.crawler.domain.model.UrlData;
import com.yamajun.crawler.crawler.domain.model.UrlStatus;
import io.vavr.control.Try;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.jongo.Jongo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Slf4j
public abstract class AbstractCrawler {

  private final ExecutorService executor;
  private final CrawlerConfig crawlerConfig;
  private final AtomicBoolean keepExecution;

  private CrawlerRepository crawlerRepository;

  public AbstractCrawler(CrawlerConfig crawlerConfig, Jongo jongo) {
    executor = Executors.newFixedThreadPool(crawlerConfig.getNumOfThreads());
    this.crawlerConfig = crawlerConfig;
    keepExecution = new AtomicBoolean(true);
    this.crawlerRepository = new CrawlerRepositoryImpl(jongo, getCrawlerName());
  }

  public final void startCrawler(){
    Crawler crawler = initializeCrawler();
    crawler.setNumOfExecutions(crawler.getNumOfExecutions() + 1);
    crawler.setLastExecutionStart(Instant.now());
    crawler.setStatus(CrawlerStatus.STARTED);
    crawlerRepository.updateCrawler(crawler);

    crawlLoop(crawler);
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

  private void crawlLoop(final Crawler crawler){
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
              newUrlData.setCrawlerParentId(crawler.get_id());
              newUrlData.setStatus(UrlStatus.NOT_CRAWLED);
              newUrlData.setUrl(url);
              crawlerRepository.addUrlData(newUrlData);
            }
          }
          var extractionDataMap = extractData(document);
          var extraction = new Extraction();
          extraction.setData(extractionDataMap);
          extraction.setUrl(targetUrl);
          crawlerRepository.addExtraction(crawler.get_id(), extraction);
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


  private Crawler initializeCrawler(){
    var crawler = crawlerRepository.findCrawlerByName(getCrawlerName());
    if(crawler == null){
      var domain = crawlerConfig.getDomainName();

      crawler = new Crawler();
      crawler.setDomain(domain);
      crawler.setName(getCrawlerName());
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

    return crawler;
  }


  private Document connect(String url) {
    try {
      return Jsoup.connect(url).get();
    } catch (IOException e) {
      throw new ConnectionException("Cannot connect to url: " + url, e);
    }
  }

  public abstract Map<String, String> extractData(Document document);

  public abstract List<String> extractUrls(Document document);

  public abstract String getCrawlerName();

  public abstract boolean canExtractInfoFromThis(Document document);

}

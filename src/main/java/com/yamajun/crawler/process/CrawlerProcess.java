package com.yamajun.crawler.process;

import static java.util.stream.Collectors.toList;

import com.yamajun.crawler.experimental.GroovyScript;
import com.yamajun.crawler.experimental.GroovyScriptExecutor;
import com.yamajun.crawler.model.Crawler;
import com.yamajun.crawler.model.CrawlerStats;
import com.yamajun.crawler.model.UrlData;
import com.yamajun.crawler.model.UrlExtraction;
import com.yamajun.crawler.model.status.CrawlerStatus;
import com.yamajun.crawler.model.status.UrlStatus;
import com.yamajun.crawler.exception.ConnectionException;
import com.yamajun.crawler.process.model.SynchronizedCrawlerStats;
import com.yamajun.crawler.repository.CrawlerProcessRepository;
import com.yamajun.crawler.repository.UrlDataRepository;
import com.yamajun.crawler.repository.UrlExtractionRepository;
import com.yamajun.crawler.utils.NormalizationUtils;
import com.yamajun.crawler.xsoup.Xsoup;
import groovy.lang.GroovyShell;
import io.vavr.control.Try;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Async;

@Slf4j
public abstract class CrawlerProcess implements CrawlerProcessSpec {

  protected final Crawler attachedCrawler;

  protected final ExecutorService executor;

  protected final UrlDataRepository urlDataRepository;
  protected final UrlExtractionRepository urlExtractionRepository;
  protected final CrawlerProcessRepository crawlerProcessRepository;

  protected final GroovyScriptExecutor scriptExecutor;

  protected final SynchronizedCrawlerStats currentStats;

  protected final AtomicBoolean keepExecution;

  CrawlerProcess(Crawler attachedCrawler,
      UrlDataRepository urlDataRepository,
      UrlExtractionRepository urlExtractionRepository,
      CrawlerProcessRepository crawlerProcessRepository) {
    this.attachedCrawler = attachedCrawler;
    executor = Executors.newFixedThreadPool(attachedCrawler.getConfig().getNumOfThreads());
    this.urlDataRepository = urlDataRepository;
    this.urlExtractionRepository = urlExtractionRepository;
    this.crawlerProcessRepository = crawlerProcessRepository;
    this.currentStats = new SynchronizedCrawlerStats();

    scriptExecutor = new GroovyScriptExecutor();
    loadCrawlerStats(attachedCrawler.getStats());
    keepExecution = new AtomicBoolean(false);
  }

  @Override
  public void init() {
    log.info("Initializing crawler process. <_id:{}>", attachedCrawler.get_id());
    log.info("Initialized crawler process. <_id:{}>", attachedCrawler.get_id());
  }

  @Override
  public final void startCrawler() {
    log.info("Starting crawler process. <_id:{}>", attachedCrawler.get_id());
    if (getStatus() != CrawlerStatus.FINISHED) {
      keepExecution.set(true);
      changeCrawlerStatus(CrawlerStatus.STARTED);
      currentStats.incNumOfExecutions();
      crawlerLoop();
    }
    log.info("Finished crawler process. <_id:{}>", attachedCrawler.get_id());
  }

  @Override
  public final void stopCrawler() {
    keepExecution.set(false);
    changeCrawlerStatus(CrawlerStatus.STOPPED);
  }

  @Override
  public Document connect(String url) {
    try {
      return Jsoup.connect(url).get();
    } catch (IOException e) {
       throw new ConnectionException("Cannot connect to url: " + url, e);
    }
  }

  @Override
  public final List<String> extractUrls(Document document) {
    var elements = document.select("a");
    var urlList = new ArrayList<String>();
    for (var element : elements) {
      urlList.add(element.attr("abs:href"));
    }

    return urlList;
  }

  @Override
  public final Map<String, Object> extractData(Document document, GroovyScript script) {
    return (Map<String, Object>) scriptExecutor.execute(script, document);
  }

  @Override
  public final boolean canExtractData(Document document, GroovyScript script) {
    return (boolean) scriptExecutor.execute(script, document);
  }

  public abstract void crawlerLoop();

  @Override
  public final synchronized CrawlerStatus getStatus() {
    return attachedCrawler.getStatus();
  }

  @Override
  public final String getCrawlerId() {
    return attachedCrawler.get_id();
  }

  /**
   * Private methods
   **/
  protected synchronized void changeCrawlerStatus(CrawlerStatus status) {
    this.attachedCrawler.setStatus(status);
    crawlerProcessRepository.updateCrawlerStatus(attachedCrawler.get_id(), status);
  }

  protected void loadCrawlerStats(CrawlerStats stats) {
    currentStats.setNumOfExecutions(stats.getNumOfExecutions());
    currentStats.setNumOfExtractions(stats.getNumOfExtractions());
    currentStats.setPagesCrawled(stats.getPagesCrawled());
  }

  protected boolean matchesWhiteList(String url, List<String> whiteList) {
    for (var keyword : whiteList) {
      if (url.contains(keyword)) {
        return true;
      }
    }

    return false;
  }

  protected Object evaluateXpath(String xpath, Document document) {
    return Xsoup.compile(xpath).evaluate(document).get();
  }
}

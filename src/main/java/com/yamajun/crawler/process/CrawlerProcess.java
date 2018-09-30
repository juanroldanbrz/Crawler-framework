package com.yamajun.crawler.process;

import static java.util.stream.Collectors.toList;

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
public class CrawlerProcess implements CrawlerProcessSpec {

  private final Crawler attachedCrawler;

  private ExecutorService executor;

  private final UrlDataRepository urlDataRepository;
  private final UrlExtractionRepository urlExtractionRepository;
  private final CrawlerProcessRepository crawlerProcessRepository;

  private final SynchronizedCrawlerStats currentStats;

  private final AtomicBoolean keepExecution;

  CrawlerProcess(Crawler attachedCrawler,
      UrlDataRepository urlDataRepository,
      UrlExtractionRepository urlExtractionRepository,
      CrawlerProcessRepository crawlerProcessRepository) {
    this.attachedCrawler = attachedCrawler;
    this.urlDataRepository = urlDataRepository;
    this.urlExtractionRepository = urlExtractionRepository;
    this.crawlerProcessRepository = crawlerProcessRepository;
    this.currentStats = new SynchronizedCrawlerStats();

    keepExecution = new AtomicBoolean(false);
  }

  @Override
  public void init() {
    loadCrawlerStats(attachedCrawler.getStats());
    executor = Executors.newFixedThreadPool(attachedCrawler.getConfig().getNumOfThreads());
    if(urlDataRepository.findNotVisitedPages(attachedCrawler.get_id(), 1).isEmpty()){
      urlDataRepository.add(UrlData.of(attachedCrawler.getConfig().getEntryPointUrl(), attachedCrawler.get_id()));
    }
  }

  @Override
  public void startCrawler() {
    if (getStatus() != CrawlerStatus.STARTED) {
      keepExecution.set(true);
      changeCrawlerStatus(CrawlerStatus.STARTED);
      currentStats.incNumOfExecutions();
      crawlerLoop();
    }
  }

  @Override
  public void stopCrawler() {
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
  public List<String> extractUrls(Document document) {
    var elements = document.select("a");
    var urlList = new ArrayList<String>();
    for (var element : elements) {
      urlList.add(element.attr("abs:href"));
    }

    return urlList;
  }

  @Override
  public Map<String, Object> extractData(Document document, Map<String, String> xpathRules) {
    var extractionMap = new HashMap<String, Object>();
    for (var operation : xpathRules.entrySet()) {
      var resultKey = operation.getKey();
      var operationXpath = operation.getKey();
      extractionMap.put(resultKey, evaluateXpath(operationXpath, document));
    }

    return extractionMap;
  }

  @Override
  public boolean canExtractData(Document document, String xpath) {
    return evaluateXpath(xpath, document) != null;
  }

  @Async
  @Override
  public void crawlerLoop() {
    var numberOfThreads = attachedCrawler.getConfig().getNumOfThreads();
    var crawlerId = attachedCrawler.get_id();
    var whiteList = attachedCrawler.getConfig().getWhiteListContains();
    var extractionRules = attachedCrawler.getConfig().getXpathExtractionRules();
    var canExtractUrlXpath = attachedCrawler.getConfig().getMagicXpath();

    var linksToCrawlUrlData = urlDataRepository.findNotVisitedPages(crawlerId, numberOfThreads);

    while (!linksToCrawlUrlData.isEmpty() && keepExecution.get()) {
      var invocations = new ArrayList<Callable<Boolean>>();
      for (final var urlData : linksToCrawlUrlData) {
        if (!keepExecution.get()) {
          break;
        }
        var urlDataId = urlData.get_id();
        var targetUrl = urlData.getUrl();
        log.info("Crawling {}", targetUrl);
        invocations.add(() -> {
          urlDataRepository.updateStatus(urlDataId, UrlStatus.CRAWLING);
          try {
            var document = connect(targetUrl);
            var urls = extractUrls(document);
            var distinctUrls = urls.stream()
                .distinct()
                .map(NormalizationUtils::normalizeUrl)
                .filter(Objects::nonNull)
                .filter(url -> matchesWhiteList(url, whiteList))
                .collect(toList());

            for (var url : distinctUrls) {
                UrlData newUrlData = UrlData.of(url, crawlerId);
                urlDataRepository.add(newUrlData);
            }

            if (canExtractData(document, canExtractUrlXpath)) {
              var extractionDataMap = extractData(document, extractionRules);
              var extraction = UrlExtraction.of(crawlerId, targetUrl, extractionDataMap);
              urlExtractionRepository.addExtraction(extraction);
              currentStats.getNumOfExtractions();
            }

            urlDataRepository.updateStatus(urlDataId, UrlStatus.CRAWLED);
            currentStats.incPagesCrawled();
            return true;
          } catch (Exception e) {
            urlDataRepository.updateStatus(urlDataId, UrlStatus.ERROR);
            return false;
          }
        });
      }

      try {
        executor.invokeAll(invocations);
      } catch (InterruptedException e) {
        log.info("Error executing threads in the crawler. <id: {}> " + attachedCrawler.get_id(), e);
      }
      linksToCrawlUrlData = urlDataRepository.findNotVisitedPages(crawlerId, numberOfThreads);
    }

    crawlerProcessRepository.updateStats(crawlerId, currentStats);
    changeCrawlerStatus(CrawlerStatus.FINISHED);
  }

  @Override
  public synchronized CrawlerStatus getStatus() {
    return attachedCrawler.getStatus();
  }

  @Override
  public String getCrawlerId() {
    return attachedCrawler.get_id();
  }

  /**
   * Private methods
   **/
  private synchronized void changeCrawlerStatus(CrawlerStatus status) {
    this.attachedCrawler.setStatus(status);
    crawlerProcessRepository.updateCrawlerStatus(attachedCrawler.get_id(), status);
  }

  private void loadCrawlerStats(CrawlerStats stats) {
    currentStats.setNumOfExecutions(stats.getNumOfExecutions());
    currentStats.setNumOfExtractions(stats.getNumOfExtractions());
    currentStats.setPagesCrawled(stats.getPagesCrawled());
  }

  private boolean matchesWhiteList(String url, List<String> whiteList) {
    for (var keyword : whiteList) {
      if (url.contains(keyword)) {
        return true;
      }
    }

    return false;
  }

  private Object evaluateXpath(String xpath, Document document) {
    return Xsoup.compile(xpath).evaluate(document).get();
  }
}
package com.yamajun.crawler.process;

import static java.util.stream.Collectors.toList;

import com.yamajun.crawler.model.Crawler;
import com.yamajun.crawler.model.UrlData;
import com.yamajun.crawler.model.UrlExtraction;
import com.yamajun.crawler.model.status.CrawlerStatus;
import com.yamajun.crawler.model.status.UrlStatus;
import com.yamajun.crawler.repository.CrawlerProcessRepository;
import com.yamajun.crawler.repository.UrlDataRepository;
import com.yamajun.crawler.repository.UrlExtractionRepository;
import com.yamajun.crawler.utils.NormalizationUtils;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrawlerProcessBruteForce extends CrawlerProcess {

  public CrawlerProcessBruteForce(Crawler attachedCrawler,
      UrlDataRepository urlDataRepository,
      UrlExtractionRepository urlExtractionRepository,
      CrawlerProcessRepository crawlerProcessRepository) {
    super(attachedCrawler, urlDataRepository, urlExtractionRepository, crawlerProcessRepository);
  }

  @Override
  public void init() {
    log.info("Initializing crawler process. <_id:{}>", attachedCrawler.get_id());
    if(urlDataRepository.findNotVisitedPages(attachedCrawler.get_id(), 1).isEmpty()){
      urlDataRepository.add(UrlData.of(attachedCrawler.getConfig().getEntryPointUrl(), attachedCrawler.get_id()));
    }
    log.info("Initialized crawler process. <_id:{}>", attachedCrawler.get_id());
  }

  @Override
  public void crawlerLoop() {
    var numberOfThreads = attachedCrawler.getConfig().getNumOfThreads();
    var crawlerId = attachedCrawler.get_id();
    var whiteList = attachedCrawler.getConfig().getWhiteListContains();
    var extractionScript = attachedCrawler.getConfig().getExtractionScript();
    var decisionScript = attachedCrawler.getConfig().getDecisionScript();

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

            if (canExtractData(document, decisionScript)) {
              var extractionDataMap = extractData(document, extractionScript);
              var extraction = UrlExtraction.of(crawlerId, targetUrl, extractionDataMap);
              urlExtractionRepository.addExtraction(extraction);
              currentStats.incNumOfExtractions();
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
      crawlerProcessRepository.updateStats(crawlerId, currentStats);
      linksToCrawlUrlData = urlDataRepository.findNotVisitedPages(crawlerId, numberOfThreads);
    }

    changeCrawlerStatus(CrawlerStatus.FINISHED);
  }
}

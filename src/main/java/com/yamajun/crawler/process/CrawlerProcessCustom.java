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
import com.yamajun.crawler.utils.UrlGeneratorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrawlerProcessCustom extends CrawlerProcess {

  public CrawlerProcessCustom(Crawler attachedCrawler,
      UrlDataRepository urlDataRepository,
      UrlExtractionRepository urlExtractionRepository,
      CrawlerProcessRepository crawlerProcessRepository) {
    super(attachedCrawler, urlDataRepository, urlExtractionRepository, crawlerProcessRepository);
  }

  @Override
  public void init() {
    log.info("Initializing crawler process. <_id:{}>", crawlerId);
    //if(urlDataRepository.findNotVisitedPages(crawlerId, 1).isEmpty()){
      var numOfThreads = attachedCrawler.getConfig().getNumOfThreads();
      for(var template : attachedCrawler.getConfig().getRegexToCrawl()){
        var nextUrlDataList = UrlGeneratorUtils.generateUrls(crawlerId, template,1, numOfThreads);
        urlDataRepository.addAll(nextUrlDataList);
      }

    log.info("Initialized crawler process. <_id:{}>", crawlerId);
  }

  @Override
  public void crawlerLoop() {
    var numberOfThreads = attachedCrawler.getConfig().getNumOfThreads();
    var extractionScript = attachedCrawler.getConfig().getExtractionScript();

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
            var extractionDataMap = extractData(document, extractionScript);
            var extraction = UrlExtraction.of(crawlerId, targetUrl, extractionDataMap);
            urlExtractionRepository.addExtraction(extraction);
            currentStats.incNumOfExtractions();

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
      var nextUrlDataList = generateNextUrlDataList();
      urlDataRepository.addAll(nextUrlDataList);
      linksToCrawlUrlData = urlDataRepository.findNotVisitedPages(crawlerId, numberOfThreads);
    }

    changeCrawlerStatus(CrawlerStatus.FINISHED);
  }

  private List<UrlData> generateNextUrlDataList(){
    return attachedCrawler.getConfig().getRegexToCrawl().stream()
        .map(regex -> urlDataRepository.findLatestByRegex(regex, crawlerId))
        .map(urlData -> UrlGeneratorUtils.generateUrls(crawlerId, urlData.getCrawlerRegex(),
            urlData.getPageNumber()+1, 1).get(0))
        .collect(Collectors.toList());
  }
}

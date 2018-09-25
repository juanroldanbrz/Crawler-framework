package com.yamajun.crawler.crawler.domain;

import com.yamajun.crawler.exception.DuplicatedCrawlerException;
import com.yamajun.crawler.crawler.domain.model.Crawler;
import com.yamajun.crawler.crawler.domain.model.Extraction;
import com.yamajun.crawler.crawler.domain.model.UrlData;
import com.yamajun.crawler.crawler.domain.model.UrlStatus;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;

public final class CrawlerRepositoryImpl implements CrawlerRepository {

  private final MongoCollection urlDataCollection;
  private final MongoCollection crawlerCollection;

  @Autowired
  public CrawlerRepositoryImpl(Jongo jongo, String crawlerName){
    this.urlDataCollection = jongo.getCollection(crawlerName + "_url_data");
    this.crawlerCollection = jongo.getCollection(crawlerName + "_crawler");
  }

  @Override
  public UrlData addUrlData(final UrlData urlData) {
    urlDataCollection.remove("{url : #}", urlData.getUrl());
    urlDataCollection.save(urlData);
    return urlData;
  }

  @Override
  public UrlData findUrlDataById(String id){
    return urlDataCollection.findOne("{ _id : #}", new ObjectId(id)).as(UrlData.class);
  }

  @Override
  public UrlData updateUrlData(final UrlData urlData) {
    var now = Instant.now();
    urlDataCollection.update("{ lastVisited : #, _id : #}", urlData.getLastVisited(), new ObjectId(urlData.get_id()))
        .with("{lastVisited : #, status : #}", now, urlData.getStatus());
    urlData.setLastVisited(now);
    return urlData;
  }

  @Override
  public List<UrlData> findNotVisitedPages(int numberOfPages) {
    var urlsData = urlDataCollection.find("{ status : #}", UrlStatus.NOT_CRAWLED)
        .limit(numberOfPages).as(UrlData.class);

    var toReturn = new ArrayList<UrlData>();
    for(var urlData : urlsData){
      urlData.setStatus(UrlStatus.CRAWLING);
      updateUrlData(urlData);
      toReturn.add(urlData);
    }

    return toReturn;
  }

  @Override
  public Crawler findCrawlerByName(String name) {
    return crawlerCollection.findOne("{name : #}", name).as(Crawler.class);
  }

  @Override
  public Crawler addCrawler(Crawler crawler) {
    if(findCrawlerByName(crawler.getName()) != null){
      throw new DuplicatedCrawlerException("Trying to add duplicate crawler: " + crawler.getName());

    }

    crawlerCollection.save(crawler);
    return crawler;
  }

  @Override
  public Crawler updateCrawler(Crawler crawler) {
    var lastUpdatedAt = Instant.ofEpochMilli(crawler.getUpdatedAt().toEpochMilli());
    crawler.setUpdatedAt(Instant.now());
    crawlerCollection.update("{ name :  #, updatedAt : #}", crawler.getName(), lastUpdatedAt).with("{ $set: "
        + "{name: #, pagesCrawled: #, numOfExtractions : #, numOfExecutions: #, status : #, lastUpdatedAt : #}"
        + "}", crawler.getName(), crawler.getPagesCrawled(), crawler.getNumOfExtractions(), crawler.getNumOfExecutions(), crawler.getStatus(), crawler.getUpdatedAt());
    return crawler;
  }

  @Override
  public void addExtraction(String crawlerId, Extraction extraction) {
    crawlerCollection.update("{ _id : # } ", new ObjectId(crawlerId))
        .with("{$push: {extractions : #}}", extraction);
  }
}

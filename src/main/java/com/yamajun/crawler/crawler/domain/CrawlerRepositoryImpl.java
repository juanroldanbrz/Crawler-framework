package com.yamajun.crawler.crawler.domain;

import com.yamajun.crawler.crawler.domain.model.CrawlerDomain;
import com.yamajun.crawler.exception.DuplicatedCrawlerException;
import com.yamajun.crawler.crawler.domain.model.Extraction;
import com.yamajun.crawler.crawler.domain.model.UrlData;
import com.yamajun.crawler.crawler.domain.model.UrlStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

public final class CrawlerRepositoryImpl implements CrawlerRepository {

  private final MongoCollection urlDataCollection;
  private final MongoCollection crawlerCollection;

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
  public CrawlerDomain findCrawlerByName(String name) {
    return crawlerCollection.findOne("{name : #}", name).as(CrawlerDomain.class);
  }

  @Override
  public CrawlerDomain addCrawler(CrawlerDomain crawlerDomain) {
    if(findCrawlerByName(crawlerDomain.getName()) != null){
      throw new DuplicatedCrawlerException("Trying to add duplicate crawlerDomain: " + crawlerDomain.getName());

    }

    crawlerCollection.save(crawlerDomain);
    return crawlerDomain;
  }

  @Override
  public CrawlerDomain updateCrawler(CrawlerDomain crawlerDomain) {
    var lastUpdatedAt = Instant.ofEpochMilli(crawlerDomain.getUpdatedAt().toEpochMilli());
    crawlerDomain.setUpdatedAt(Instant.now());
    crawlerCollection.update("{ name :  #, updatedAt : #}", crawlerDomain.getName(), lastUpdatedAt).with("{ $set: "
        + "{name: #, pagesCrawled: #, numOfExtractions : #, numOfExecutions: #, status : #, lastUpdatedAt : #}"
        + "}", crawlerDomain.getName(), crawlerDomain.getPagesCrawled(), crawlerDomain.getNumOfExtractions(), crawlerDomain
        .getNumOfExecutions(), crawlerDomain.getStatus(), crawlerDomain.getUpdatedAt());
    return crawlerDomain;
  }

  @Override
  public List<CrawlerDomain> findAllCrawler() {
    var it = crawlerCollection.find("{}, {extractions : -1}").as(CrawlerDomain.class).iterator();
    var toReturn = new ArrayList<CrawlerDomain>();
    it.forEachRemaining(toReturn::add);
    return toReturn;
  }

  @Override
  public void addExtraction(String crawlerId, Extraction extraction) {
    crawlerCollection.update("{ _id : # } ", new ObjectId(crawlerId))
        .with("{$push: {extractions : #}}", extraction);
  }
}

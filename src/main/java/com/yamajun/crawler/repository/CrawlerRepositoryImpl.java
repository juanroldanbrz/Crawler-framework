package com.yamajun.crawler.repository;

import com.yamajun.crawler.model.Crawler;
import com.yamajun.crawler.model.CrawlerConfig;
import com.yamajun.crawler.model.status.CrawlerStatus;
import com.yamajun.crawler.process.model.SynchronizedCrawlerStats;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CrawlerRepositoryImpl implements CrawlerProcessRepository, CrawlerRepository {

  private final MongoCollection collection;

  @Autowired
  public CrawlerRepositoryImpl(Jongo jongo) {
    this.collection = jongo.getCollection("crawler");
  }

  @Override
  public void updateStats(String crawlerId, SynchronizedCrawlerStats stats) {
    collection.update(new ObjectId(crawlerId)).with("{$set:{stats : #}}", stats);
  }

  @Override
  public void updateCrawlerStatus(String crawlerId, CrawlerStatus status) {
    collection.update(new ObjectId(crawlerId)).with("{$set:{status : #}}", status);
  }

  @Override
  public List<Crawler> findAll() {
    var iterator = collection.find().as(Crawler.class).iterator();
    var toReturn = new ArrayList<Crawler>();
    iterator.forEachRemaining(toReturn::add);
    return toReturn;
  }

  @Override
  public Crawler findById(String id) {
    return collection.findOne(new ObjectId(id)).as(Crawler.class);
  }

  @Override
  public Crawler save(Crawler crawler) {
    collection.save(crawler);
    return crawler;
  }

  @Override
  public void remove(String crawlerId) {
    collection.remove(new ObjectId(crawlerId));
  }

  @Override
  public void updateConfig(String crawlerId, CrawlerConfig config) {
    collection.update(new ObjectId(crawlerId)).with("{$set:{config : #}}", config);
  }
}

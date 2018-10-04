package com.yamajun.crawler.repository;

import com.yamajun.crawler.model.UrlData;
import com.yamajun.crawler.model.status.UrlStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class UrlDataRepositoryImpl implements UrlDataRepository{

  private final MongoCollection collection;

  @Autowired
  public UrlDataRepositoryImpl(Jongo jongo) {
    this.collection = jongo.getCollection("url_data");
  }

  @Override
  public UrlData findById(String id) {
    return collection.findOne(new ObjectId(id)).as(UrlData.class);
  }

  @Override
  public UrlData add(UrlData urlData) {
    if(collection.findOne("{url:#}", urlData.getUrl()).as(UrlData.class) == null){
      urlData.setCreatedAt(Instant.now());
      collection.save(urlData);
    } else {
      log.debug("Not saving duplicated url. <url: {}>", urlData.getUrl());
    }
    return urlData;
  }

  @Override
  public List<UrlData> addAll(List<UrlData> urlDataList) {
    urlDataList.forEach(this::add);
    return urlDataList;
  }

  @Override
  public UrlData updateStatus(String urlDataId, UrlStatus status) {
    collection.update(new ObjectId(urlDataId)).with("{$set: {status:#}}", status);
    return null;
  }

  @Override
  public List<UrlData> findNotVisitedPages(String crawlerId, int numberOfPages) {
    var iterator = collection.find("{crawlerParentId: #, status:#}",crawlerId, UrlStatus.NOT_CRAWLED).limit(numberOfPages).as(UrlData.class);
    var toReturn = new ArrayList<UrlData>();
    iterator.forEachRemaining(toReturn::add);
    return toReturn;
  }

  @Override
  public UrlData findLatestByRegex(String regex, String crawlerId) {
    return collection.find("{crawlerRegex:#,crawlerParentId: #}", regex, crawlerId).sort("{createdAt: -1}").limit(1).as(UrlData.class).next();
  }
}

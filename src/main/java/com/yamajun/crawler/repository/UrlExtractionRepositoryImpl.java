package com.yamajun.crawler.repository;

import com.yamajun.crawler.model.UrlExtraction;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UrlExtractionRepositoryImpl implements UrlExtractionRepository {

  private final MongoCollection collection;

  @Autowired
  public UrlExtractionRepositoryImpl(Jongo jongo) {
    this.collection = jongo.getCollection("extractions");
  }

  @Override
  public UrlExtraction addExtraction(UrlExtraction urlExtraction) {
    collection.save(urlExtraction);
    return urlExtraction;
  }
}

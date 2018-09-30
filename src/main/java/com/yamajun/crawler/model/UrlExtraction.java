package com.yamajun.crawler.model;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.jongo.marshall.jackson.oid.MongoObjectId;

@Getter
@Setter
public class UrlExtraction {

  @MongoObjectId
  private String _id;

  private String crawlerId;
  private String url;
  private Map<String, Object> data;

  public static UrlExtraction of(String crawlerId, String url, Map<String, Object> data) {
    UrlExtraction urlExtraction = new UrlExtraction();
    urlExtraction.setCrawlerId(crawlerId);
    urlExtraction.setUrl(url);
    urlExtraction.setData(data);
    return urlExtraction;
  }
}

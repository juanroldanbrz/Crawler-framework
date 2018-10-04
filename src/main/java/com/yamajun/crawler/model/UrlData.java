package com.yamajun.crawler.model;

import com.yamajun.crawler.model.status.UrlStatus;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.jongo.marshall.jackson.oid.MongoObjectId;

@Getter
@Setter
public class UrlData {

  @MongoObjectId
  private String _id;

  private String url;
  private String crawlerParentId;
  private UrlStatus status;
  private Integer pageNumber;
  private String crawlerRegex;
  private Instant createdAt;

  public static UrlData of(String url, String crawlerParentId){
    UrlData urlData = new UrlData();
    urlData.setUrl(url);
    urlData.setCrawlerParentId(crawlerParentId);
    urlData.setStatus(UrlStatus.NOT_CRAWLED);
    return urlData;
  }

  public static UrlData of(String url, String crawlerParentId, String crawlerRegex, int pageNumber) {
    UrlData urlData = of(url, crawlerParentId);
    urlData.setCrawlerRegex(crawlerRegex);
    urlData.setPageNumber(pageNumber);
    return urlData;
  }

}

package com.yamajun.crawler.model;

import com.yamajun.crawler.model.status.CrawlerStatus;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.jongo.marshall.jackson.oid.MongoObjectId;

@Getter
@Setter
public class Crawler {

  @MongoObjectId
  private String _id;
  private String crawlerName;
  private CrawlerStatus status = CrawlerStatus.CREATED;
  private CrawlerType type;

  private CrawlerConfig config;
  private CrawlerStats stats = new CrawlerStats();
}

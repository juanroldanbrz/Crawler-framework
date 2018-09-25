package com.yamajun.crawler.crawler.domain.model;

import java.sql.Timestamp;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

@Getter
@Setter
public class UrlData {

  @MongoObjectId
  private String _id;

  private String url;
  private String crawlerParentId;
  private UrlStatus status;
  private Instant lastVisited;

}

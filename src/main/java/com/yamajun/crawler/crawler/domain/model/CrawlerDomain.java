package com.yamajun.crawler.crawler.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yamajun.crawler.crawler.CrawlerConfig;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

@Getter
@Setter
public class CrawlerDomain {

  @MongoObjectId
  private String _id;

  private String name;
  private String domain;
  private long pagesCrawled;
  private long numOfExtractions;
  private long numOfExecutions;

  private CrawlerStatus status;

  private List<Extraction> extractions = new ArrayList<>();

  private CrawlerConfig crawlerConfig;

  private Instant createdAt;
  private Instant updatedAt;
  private Instant lastExecutionStart;

}

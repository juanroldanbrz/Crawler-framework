package com.yamajun.crawler.experimental;

import com.yamajun.crawler.crawler.CrawlerConfig;
import com.yamajun.crawler.crawler.domain.model.CrawlerStatus;
import com.yamajun.crawler.crawler.domain.model.Extraction;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.jongo.marshall.jackson.oid.MongoObjectId;

@Getter
@Setter
public class CrawlerDefinition {

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

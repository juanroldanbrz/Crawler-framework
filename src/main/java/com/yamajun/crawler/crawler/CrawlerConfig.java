package com.yamajun.crawler.crawler;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.jongo.marshall.jackson.oid.MongoObjectId;

@Getter
@Setter
public class CrawlerConfig {

  @MongoObjectId
  private String _id;

  private String crawlerName;

  private String domainName;
  private String entryPointUrl;
  private int numOfThreads = 1;

  private List<String> whiteListContains;
  private Map<String, String> xpathExtractionRules;

  //This xpath will decide if the document can be extracted
  private String magicXpath;

  private Instant lastModification;

}

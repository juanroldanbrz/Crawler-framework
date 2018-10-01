package com.yamajun.crawler.model;

import com.yamajun.crawler.experimental.GroovyScript;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.jongo.marshall.jackson.oid.MongoObjectId;

@Getter
@Setter
public class CrawlerConfig {

  private String domainName;
  private String entryPointUrl;
  private int numOfThreads = 1;

  private List<String> whiteListContains;

  private GroovyScript decisionScript;
  private GroovyScript extractionScript;
  private Instant lastModification;

}

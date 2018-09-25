package com.yamajun.crawler.crawler.domain.model;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

@Getter
@Setter
public class Extraction {

  @MongoObjectId
  private String _id;

  private String url;
  private Map<String, String> data;

}

package com.yamajun.crawler.experimental;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroovyScript {

  private String functionName;
  private String content;
  private Class returnType;
  private Type type;

  public enum Type {
    DECISION,
    EXTRACTION
  }
}

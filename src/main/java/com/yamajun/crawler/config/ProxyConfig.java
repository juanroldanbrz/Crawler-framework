package com.yamajun.crawler.config;

import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyConfig {

  @PostConstruct
  public void postInit(){
    System.setProperty("https.proxyHost", "proxylb.internal.epo.org");
    System.setProperty("https.proxyPort", "8080");
  }
}

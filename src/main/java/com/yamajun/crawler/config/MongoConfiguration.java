package com.yamajun.crawler.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.MongoClient;
import org.jongo.Jongo;
import org.jongo.marshall.jackson.JacksonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfiguration {

  @Bean
  public Jongo getDb(MongoClient mongoClient) {
    return new Jongo(mongoClient.getDB("crawler"), getBuilder().build());
  }


  private JacksonMapper.Builder getBuilder() {
    JacksonMapper.Builder tmpMapper = new JacksonMapper.Builder();
    for (Module module : ObjectMapper.findModules()) {
      tmpMapper.registerModule(module);
    }
    tmpMapper.enable(MapperFeature.AUTO_DETECT_GETTERS);
    tmpMapper.registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module());
    return tmpMapper;
  }


}

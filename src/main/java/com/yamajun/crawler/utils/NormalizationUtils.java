package com.yamajun.crawler.utils;

import io.vavr.control.Try;
import java.net.URL;

public class NormalizationUtils {

  public static String normalizeUrl(String url){
    var attempNormalize = Try.of(() -> new URL(url));
    if(attempNormalize.isFailure()){
      return null;
    } else {
      var urlWrap = attempNormalize.get();
      return urlWrap.getProtocol() + "://" + urlWrap.getHost() + urlWrap.getFile();
    }
  }
}

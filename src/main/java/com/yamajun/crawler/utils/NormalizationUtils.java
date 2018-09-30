package com.yamajun.crawler.utils;

import io.vavr.control.Try;
import java.net.URL;

public class NormalizationUtils {

  public static String normalizeUrl(String url){
    var attemptNormalize = Try.of(() -> new URL(url));
    if(attemptNormalize.isFailure()){
      return null;
    } else {
      var urlWrap = attemptNormalize.get();
      return urlWrap.getProtocol() + "://" + urlWrap.getHost() + urlWrap.getFile();
    }
  }
}

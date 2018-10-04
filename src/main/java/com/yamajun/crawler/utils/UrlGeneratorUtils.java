package com.yamajun.crawler.utils;

import com.yamajun.crawler.model.UrlData;
import java.util.ArrayList;
import java.util.List;

public class UrlGeneratorUtils {

  public static List<UrlData> generateUrls(String crawlerParentId, String urlTemplate, int offset, int pageNumbersToCreate){
    var toReturn = new ArrayList<UrlData>();
    for(int i = 0; i < pageNumbersToCreate; i++){
      var pageNumber = i + offset;
      var url = String.format(urlTemplate, String.valueOf(pageNumber));
      var urlData = UrlData.of(url, crawlerParentId, urlTemplate, pageNumber);
      toReturn.add(urlData);
    }
    return toReturn;
  }

}

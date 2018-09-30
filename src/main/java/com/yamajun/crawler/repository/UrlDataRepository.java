package com.yamajun.crawler.repository;

import com.yamajun.crawler.model.UrlData;
import com.yamajun.crawler.model.status.UrlStatus;
import java.util.List;

public interface UrlDataRepository {

  UrlData findById(String id);

  UrlData add(UrlData urlData);

  UrlData updateStatus(String urlDataId, UrlStatus status);

  List<UrlData> findNotVisitedPages(String crawlerId, int numberOfPages);

}

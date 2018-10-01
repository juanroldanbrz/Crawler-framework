package com.yamajun.crawler.service.test;

import java.util.Map;

public interface TestService {

  Map<String, Object> testExtractionScript(String url, String scriptContent);

  boolean testDecisionScript(String url, String scriptContent);

}

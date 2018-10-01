package com.yamajun.crawler.service.test;

import com.yamajun.crawler.exception.ConnectionException;
import com.yamajun.crawler.experimental.GroovyScript;
import com.yamajun.crawler.experimental.GroovyScript.Type;
import com.yamajun.crawler.experimental.GroovyScriptExecutor;
import java.io.IOException;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {

  @Override
  public Map<String, Object> testExtractionScript(String url, String scriptContent) {
    var script = new GroovyScript();
    script.setContent(scriptContent);
    script.setType(Type.EXTRACTION);
    var executor = new GroovyScriptExecutor();
    Document document = null;
    try {
       document = Jsoup.connect(url).get();
    } catch (IOException e) {
      throw new ConnectionException("Excetion connectiong to the url");
    }

    return (Map<String, Object>) executor.execute(script, document);

  }

  @Override
  public boolean testDecisionScript(String url, String scriptContent) {
    var script = new GroovyScript();
    script.setContent(scriptContent);
    script.setType(Type.DECISION);
    var executor = new GroovyScriptExecutor();
    Document document = null;
    try {
      document = Jsoup.connect(url).get();
    } catch (IOException e) {
      throw new ConnectionException("Excetion connectiong to the url");
    }

    return (boolean) executor.execute(script, document);

  }
}

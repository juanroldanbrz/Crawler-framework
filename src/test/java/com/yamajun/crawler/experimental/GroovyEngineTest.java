package com.yamajun.crawler.experimental;

import static org.junit.Assert.*;

import com.yamajun.crawler.model.Crawler;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class GroovyEngineTest {

  @Test
  public void testme() throws IOException {

    var script = new GroovyScript();
    script.setContent("Map<String, String> extract(Document document){ def map = ['ok' : 'yes']; return map }");
    script.setFunctionName("extract");

    var executor = new GroovyScriptExecutor();
    var content = Jsoup.connect("http://gnula.biz/pelicula/el-rascacielos").get();
    var result = executor.execute(script, content);


  }

}
package com.yamajun.crawler;

import com.yamajun.crawler.experimental.CrawlerTest;
import com.yamajun.crawler.experimental.GnulaScript
import org.jsoup.Jsoup;
import org.junit.Test
import org.springframework.util.Assert;

public class CrawlerScriptTest implements CrawlerTest {

  @Test
  @Override
  public void testDecisionFunction(){
    def gnulaScript = new GnulaScript();
    def document = Jsoup.connect('http://gnula.biz/pelicula/el-rascacielos').get()
    Assert.isTrue(gnulaScript.decideIfCrawl(document), "This movie should be crawled")
    document = Jsoup.connect('http://gnula.biz/').get()
    Assert.isTrue(!gnulaScript.decideIfCrawl(document), "This movie should NOT be crawled")
  }

  @Test
  @Override
  public void testExtractionFunction() {
    def gnulaScript = new GnulaScript();
    def document = Jsoup.connect('http://gnula.biz/pelicula/el-rascacielos').get()
    def map = gnulaScript.extractData(document);
  }
}

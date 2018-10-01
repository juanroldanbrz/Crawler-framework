package com.yamajun.crawler.experimental;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.jsoup.nodes.Document;

public class GroovyScriptExecutor {

  private static final String SCRIPT_TEMPLATE_EXTRACTION = "import org.jsoup.Jsoup;"
      + "import org.jsoup.nodes.Document;%s;return extractData(document);";

  private static final String SCRIPT_TEMPLATE_DECISION = "import org.jsoup.Jsoup;"
      + "import org.jsoup.nodes.Document;%s;return decideIfCrawl(document);";


  private final GroovyShell shell;
  private final Binding sharedData;

  public GroovyScriptExecutor() {
    this.sharedData = new Binding();
    this.shell = new GroovyShell(sharedData);
  }

  public Object execute(GroovyScript script, Document document){
    sharedData.setVariable("document", document);
    var template = "";
    switch (script.getType()){
      case EXTRACTION:
        template = SCRIPT_TEMPLATE_EXTRACTION;
        break;
      case DECISION:
        template = SCRIPT_TEMPLATE_DECISION;
        break;
        default:
          throw new UnsupportedOperationException("Cannot perform this op");
    }
    var executorScript = String.format(template, script.getContent());
    return shell.evaluate(executorScript);
  }
}

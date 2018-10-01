package com.yamajun.crawler.experimental;

import groovy.lang.GroovyShell;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;

import groovy.lang.GroovyObjectSupport;
public class GroovyEngine {

  private final GroovyShell groovyShell;


  public GroovyEngine() {
    this.groovyShell = new GroovyShell();
  }

  public void testScript(){
    var result = groovyShell.evaluate("3*5");
  }
}

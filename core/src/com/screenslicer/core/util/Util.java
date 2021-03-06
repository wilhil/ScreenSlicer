/* 
 * ScreenSlicer (TM) -- automatic, zero-config web scraping (TM)
 * Copyright (C) 2013-2014 Machine Publishers, LLC
 * ops@machinepublishers.com | screenslicer.com | machinepublishers.com
 * 717 Martin Luther King Dr W Ste I, Cincinnati, Ohio 45220
 *
 * You can redistribute this program and/or modify it under the terms of the
 * GNU Affero General Public License version 3 as published by the Free
 * Software Foundation. Additional permissions or commercial licensing may be
 * available--see LICENSE file or contact Machine Publishers, LLC for details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License version 3
 * for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * version 3 along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * For general details about how to investigate and report license violations,
 * please see: https://www.gnu.org/licenses/gpl-violation.html
 * and email the author: ops@machinepublishers.com
 * Keep in mind that paying customers have more rights than the AGPL alone offers.
 */
package com.screenslicer.core.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.ConfirmHandler;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.OnbeforeunloadHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.RefreshHandler;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.StatusHandler;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.ibm.icu.text.CharsetDetector;
import com.screenslicer.api.datatype.HtmlNode;
import com.screenslicer.api.datatype.UrlTransform;
import com.screenslicer.common.CommonUtil;
import com.screenslicer.common.Log;
import com.screenslicer.core.scrape.Scrape.ActionFailed;
import com.screenslicer.core.scrape.type.Result;
import com.screenslicer.core.scrape.type.ScriptEngine;
import com.screenslicer.core.service.HttpStatus;
import com.screenslicer.webapp.WebApp;

public class Util {

  public static String[] control = new String[] { "a", "input", "button", "div", "li", "span", "footer" };
  private static final String[] blocks = new String[] { "address", "article", "aside", "audio", "blockquote", "canvas", "dd", "div", "dl", "fieldset", "figcaption", "figure", "footer", "form", "h1",
      "h2", "h3", "h4", "h5", "h6", "header", "hgroup", "hr", "noscript", "ol", "output", "p", "pre", "section", "table", "tr", "tfoot", "ul", "video" };
  private static final String[] proximityBlocks = new String[] { "address", "article", "aside", "audio", "blockquote", "canvas", "dd", "div", "dl", "fieldset", "figcaption", "figure", "footer",
      "form", "h1", "h2", "h3", "h4", "h5", "h6", "header", "hgroup", "hr", "noscript", "ol", "output", "p", "pre", "section", "table", "tr", "td", "tfoot", "ul", "video" };
  private static final String[] items = new String[] { "address", "article", "dd", "dt", "div", "p", "section", "table", "tr", "li", "td", "fieldset", "form", "h1", "h2", "h3", "h4", "h5", "h6", };
  private static final String[] content = new String[] { "p", "ol", "ul", "dl", "div", "colgroup", "tbody", "td", "section", "main", "form" };
  private static final String[] formatting = new String[] { "article", "h1", "h2", "h3", "h4", "h5", "h6", "header", "footer", "address", "p", "hr", "blockquote", "dt", "dd", "div", "a", "em",
      "strong", "small", "cite", "q", "dfn", "abbr", "time", "var", "i", "b", "u", "mark", "bdi", "bdo", "span", "br", "wbr", "img", "video", "audio", "source", "track", "svg", "ol", "ul", "li",
      "dl", "table", "caption", "colgroup", "col", "tbody", "thead", "tfoot", "tr", "td", "th", "section", "main" };
  private static final String[] decoration = new String[] { "abbr", "acronym", "address", "applet", "area", "aside", "b", "bdi", "bdo", "big", "blink", "br", "caption", "cite",
      "code", "data", "datalist", "em", "embed", "figcaption", "figure", "font", "i", "img", "kbd", "label", "link", "map", "mark", "marquee", "meta", "meter", "nobr",
      "noframes", "noscript", "output", "param", "plaintext", "pre", "q", "rp", "rt", "ruby", "s", "samp", "script", "small", "source", "spacer", "span", "strike", "strong", "style", "sub",
      "summary", "sup", "template", "#text", "time", "var", "video", "wbr" };
  public static final String[] parentHolder = new String[] { "ul", "ol", "div", "p", "span", "form", "td", "dl", "dd", "footer", "header", "section", "article", "blockquote", "main", "h1", "h2",
      "h3", "h4", "h5", "h6" };
  private static final int REFRESH_TRIES = 3;
  private static final String[] unbound = new String[] { "p", "dt", "dd", "tr", "table", "h1", "h2", "h3", "h4", "h5", "h6" };
  private static final String schemeFragment = "^[A-Za-z0-9]*:?(?://)?";
  private static final String NODE_MARKER = "fftheme_";
  private static final Pattern nodeMarker = Pattern.compile(NODE_MARKER + "\\d+");
  private static final String HIDDEN_MARKER = "xmoztheme";
  private static final String FILTERED_MARKER = "o2xtheme";
  private static final Pattern filteredMarker = Pattern.compile(FILTERED_MARKER);
  private static final String FILTERED_LENIENT_MARKER = "o2x2theme";
  private static final Pattern filteredLenientMarker = Pattern.compile(FILTERED_LENIENT_MARKER);
  private static final Pattern hiddenMarker = Pattern.compile(HIDDEN_MARKER);
  /*
   * used because WebElement.isDisplayed() is way too slow
   */
  private static final String isVisible =
      "      function isCurrentlyVisible(el, rect) {"
          + "  var eap,"
          + "  docEl = document.documentElement,"
          + "  vWidth = docEl.clientWidth,"
          + "  vHeight = docEl.clientHeight,"
          + "  efp = function (x, y) { return document.elementFromPoint(x, y) },"
          + "  contains = \"contains\" in el ? \"contains\" : \"compareDocumentPosition\","
          + "  has = contains == \"contains\" ? 1 : 0x14;"
          + "  if(rect.right < 0 || rect.bottom < 0 || rect.left > vWidth || rect.top > vHeight)"
          + "    return false;"
          + "  return ((eap = efp(rect.left,  rect.top)) == el || el[contains](eap) == has"
          + "    || (eap = efp(rect.right, rect.top)) == el || el[contains](eap) == has"
          + "    || (eap = efp(rect.right, rect.bottom)) == el || el[contains](eap) == has"
          + "    || (eap = efp(rect.left,  rect.bottom)) == el || el[contains](eap) == has"
          + "    || (eap = efp((rect.left+rect.right)/2,  rect.top)) == el || el[contains](eap) == has"
          + "    || (eap = efp((rect.left+rect.right)/2,  rect.bottom)) == el || el[contains](eap) == has"
          + "    || (eap = efp(rect.left,  (rect.top+rect.bottom)/2)) == el || el[contains](eap) == has"
          + "    || (eap = efp(rect.right,  (rect.top+rect.bottom)/2)) == el || el[contains](eap) == has"
          + "    || (eap = efp((rect.left+rect.right)/2,  (rect.top+rect.bottom)/2)) == el || el[contains](eap) == has);"
          + "}"
          + "function isVisible(element) {"
          + "  var rect = element.getBoundingClientRect();"
          + "  window.scrollBy(rect.left, rect.top);"
          + "  rect = element.getBoundingClientRect();"
          + "  if(isCurrentlyVisible(element, rect)){"
          + "    return true;"
          + "  }"
          + "  window.scrollByLines(-10);"
          + "  rect = element.getBoundingClientRect();"
          + "  return isCurrentlyVisible(element, rect);"
          + "}";
  public static final Pattern uriScheme = Pattern.compile("^[A-Za-z0-9]*:.*$");
  public static final boolean VERBOSE = false;
  public static final boolean DEBUG = false;
  private static Pattern attributes = Pattern.compile("(?<=<\\w{1,15}\\s)[^>]+(?=>)", Pattern.UNICODE_CHARACTER_CLASS);
  private static int STARTUP_WAIT_MS = 100;
  private static int LONG_WAIT_MS = 5837;
  private static int SHORT_WAIT_MS = 1152;
  private static int SHORT_WAIT_MIN_MS = 3783;
  private static int VERY_SHORT_WAIT_MS = 381;
  private static int VERY_SHORT_WAIT_MIN_MS = 327;
  private static int RAND_MIN_WAIT_MS = 7734;
  private static int RAND_WAIT_MS = 3734;
  private static int RAND_MAX_WAIT_MS = 129 * 1000;
  private static int RAND_MIN_WAIT_ITER = 1;
  private static int RAND_MAX_WAIT_ITER = 5;
  private static final Map<String, Integer> distCache = new HashMap<String, Integer>();
  private static final int MAX_DIST_CACHE = 4000;
  private static final SecureRandom rand = new SecureRandom();
  static {
    Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
    LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
        "org.apache.commons.logging.impl.NoOpLog");
  }

  public static void driverSleepStartup() {
    try {
      Thread.sleep(STARTUP_WAIT_MS);
    } catch (InterruptedException e) {}
  }

  public static void get(RemoteWebDriver driver, String url, Node urlNode, boolean retry) throws ActionFailed {
    boolean exception = true;
    boolean success = true;
    String origHandle = null;
    try {
      String source = null;
      boolean badUrl = true;
      boolean statusFail = true;
      if (urlNode != null) {
        origHandle = driver.getWindowHandle();
      }
      for (int i = 0; i < REFRESH_TRIES
          && (badUrl || statusFail || exception || CommonUtil.isEmpty(source)); i++) {
        badUrl = false;
        statusFail = false;
        exception = false;
        source = null;
        if (WebApp.DEBUG) {
          System.out.println("getting url...");
        }
        try {
          driver.getKeyboard().sendKeys(Keys.ESCAPE);
        } catch (Throwable t) {
          Log.exception(t);
        }
        if (urlNode != null) {
          try {
            cleanUpNewWindows(driver, origHandle);
          } catch (Throwable t) {
            Log.exception(t);
          }
        }
        try {
          Util.driverSleepVeryShort();
          if (urlNode != null) {
            try {
              Util.clickToNewWindow(driver, toElement(driver, urlNode));
              Set<String> newHandles = driver.getWindowHandles();
              for (String newHandle : newHandles) {
                if (!origHandle.equals(newHandle)) {
                  driver.switchTo().window(newHandle);
                  break;
                }
              }
            } catch (Throwable t) {
              exception = true;
              Log.exception(t);
              Util.cleanUpNewWindows(driver, origHandle);
            }
          } else {
            driver.get("about:blank");
            try {
              driver.get(url);
            } catch (TimeoutException e) {
              Log.exception(e);
            }
          }
          if (!exception) {
            Util.driverSleepShort();
            Util.driverSleepLong();
            statusFail = HttpStatus.status(driver, urlNode != null) != 200;
            driver.switchTo().defaultContent();
            source = driver.getPageSource();
            try {
              new URL(driver.getCurrentUrl());
              badUrl = false;
            } catch (Throwable t) {
              badUrl = true;
            }
          }
        } catch (Throwable t) {
          Log.exception(t);
          exception = true;
        }
        if ((!retry || i + 1 == REFRESH_TRIES)
            && (badUrl || statusFail || exception || CommonUtil.isEmpty(source))) {
          try {
            driver.getKeyboard().sendKeys(Keys.ESCAPE);
            Util.driverSleepVeryShort();
          } catch (Throwable t) {
            Log.exception(t);
          }
          success = false;
          if (!retry) {
            break;
          }
        }
      }
      if (WebApp.DEBUG) {
        System.out.println("getting url - done");
      }
    } catch (Throwable t) {
      Log.exception(t);
      success = false;
    }
    if (!success) {
      if (urlNode != null && origHandle != null) {
        try {
          cleanUpNewWindows(driver, origHandle);
        } catch (Throwable t) {
          Log.exception(t);
        }
      }
      throw new ActionFailed();
    }
  }

  public static void get(RemoteWebDriver driver, String url, boolean retry) throws ActionFailed {
    get(driver, url, null, retry);
  }

  public static String newWindow(RemoteWebDriver driver) throws ActionFailed {
    try {
      String origHandle = driver.getWindowHandle();
      cleanUpNewWindows(driver, origHandle);
      try {
        driver.getKeyboard().sendKeys(Keys.chord(Keys.CONTROL + "n"));
      } catch (Throwable t) {
        Log.exception(t);
      }
      Util.driverSleepStartup();
      Collection<String> handles = new HashSet<String>(driver.getWindowHandles());
      handles.remove(origHandle);
      if (!handles.isEmpty()) {
        driver.switchTo().window(handles.iterator().next());
      } else {
        driver.executeScript("window.open('');");
        Util.driverSleepStartup();
        handles = new HashSet<String>(driver.getWindowHandles());
        handles.remove(origHandle);
        if (!handles.isEmpty()) {
          driver.switchTo().window(handles.iterator().next());
        }
      }
      return driver.getWindowHandle();
    } catch (Throwable t) {
      Log.exception(t);
      throw new ActionFailed(t);
    }
  }

  public static void cleanUpNewWindows(RemoteWebDriver driver, String handleToKeep) throws ActionFailed {
    try {
      Set<String> handles = new HashSet<String>(driver.getWindowHandles());
      for (String handle : handles) {
        try {
          if (!handleToKeep.equals(handle)) {
            driver.switchTo().window(handle);
            driver.close();
          }
        } catch (Throwable t) {
          Log.exception(t);
        }
      }
      driver.switchTo().window(handleToKeep);
      driver.switchTo().defaultContent();
    } catch (Throwable t) {
      Log.exception(t);
      throw new ActionFailed(t);
    }
  }

  public static String charset(String text) {
    String charset = null;
    try {
      CharsetDetector cd = new CharsetDetector();
      cd.enableInputFilter(true);
      cd.setText(text.getBytes("utf-8"));
      charset = cd.detect().getName();
    } catch (Throwable t) {
      charset = null;
      Log.exception(t);
    }
    charset = charset == null ? "utf-8" : charset;
    return charset;
  }

  public static void driverSleepRandLong() {
    try {
      int cur = RAND_MAX_WAIT_MS;
      int iter = rand.nextInt(RAND_MAX_WAIT_ITER) + RAND_MIN_WAIT_ITER;
      for (int i = 0; i < iter; i++) {
        cur = rand.nextInt(cur + 1);
      }
      Thread.sleep(rand.nextInt(cur + 1) + rand.nextInt(RAND_WAIT_MS) + RAND_MIN_WAIT_MS);
    } catch (InterruptedException e) {}
  }

  public static void driverSleepShort() {
    try {
      Thread.sleep(rand.nextInt(SHORT_WAIT_MS) + SHORT_WAIT_MIN_MS);
    } catch (InterruptedException e) {}
  }

  public static void driverSleepVeryShort() {
    try {
      Thread.sleep(rand.nextInt(VERY_SHORT_WAIT_MS) + VERY_SHORT_WAIT_MIN_MS);
    } catch (InterruptedException e) {}
  }

  public static void driverSleepLong() {
    try {
      Thread.sleep(LONG_WAIT_MS);
    } catch (InterruptedException e) {}
  }

  public static String urlFromAttr(Node node) {
    for (Attribute attr : node.attributes().asList()) {
      if (attr.getValue().contains("://")) {
        return attr.getValue();
      }
    }
    return null;
  }

  public static String stripAttributes(String str, boolean stripAllSpaces) {
    String ret = CommonUtil.strip(str, false);
    ret = attributes.matcher(ret).replaceAll("");
    if (stripAllSpaces) {
      return ret.replace(" ", "");
    }
    return ret;
  }

  public static int trimmedLen(String str) {
    if (str.isEmpty()) {
      return 0;
    }
    int count = 0;
    boolean prevWhitespace = false;
    str = str.replaceAll("&nbsp;", " ").replaceAll("&amp;nbsp;", " ").trim();
    for (int i = 0; i < str.length(); i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        ++count;
        prevWhitespace = false;
      } else if (!prevWhitespace) {
        ++count;
        prevWhitespace = true;
      }
    }
    return count;
  }

  public static WebClient newWebClient() {
    WebClient webClient = new WebClient();
    webClient.setJavaScriptEngine(new ScriptEngine(webClient));
    webClient.getOptions().setJavaScriptEnabled(false);
    webClient.getOptions().setRedirectEnabled(true);
    webClient.getOptions().setUseInsecureSSL(true);
    webClient.getOptions().setCssEnabled(false);
    webClient.getOptions().setThrowExceptionOnScriptError(false);
    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    webClient.setJavaScriptTimeout(1);
    webClient.getOptions().setActiveXNative(false);
    webClient.getOptions().setAppletEnabled(false);
    webClient.getOptions().setGeolocationEnabled(false);
    webClient.getOptions().setPopupBlockerEnabled(true);
    webClient.setRefreshHandler(new RefreshHandler() {
      @Override
      public void handleRefresh(Page page, URL url, int seconds) throws IOException {}
    });
    webClient.setAlertHandler(new AlertHandler() {
      @Override
      public void handleAlert(Page page, String message) {}
    });
    webClient.setConfirmHandler(new ConfirmHandler() {
      @Override
      public boolean handleConfirm(Page page, String message) {
        return true;
      }
    });
    webClient.setStatusHandler(new StatusHandler() {
      @Override
      public void statusMessageChanged(Page page, String message) {}
    });
    webClient.setOnbeforeunloadHandler(new OnbeforeunloadHandler() {
      @Override
      public boolean handleEvent(Page page, String returnValue) {
        return true;
      }
    });
    webClient.setCssErrorHandler(new ErrorHandler() {
      @Override
      public void error(CSSParseException arg0) throws CSSException {}

      @Override
      public void fatalError(CSSParseException arg0) throws CSSException {}

      @Override
      public void warning(CSSParseException arg0) throws CSSException {}
    });
    webClient.setIncorrectnessListener(new IncorrectnessListener() {
      @Override
      public void notify(String message, Object origin) {}
    });
    webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {
      @Override
      public void timeoutError(HtmlPage arg0, long arg1, long arg2) {}

      @Override
      public void scriptException(HtmlPage arg0, ScriptException arg1) {}

      @Override
      public void malformedScriptURL(HtmlPage arg0, String arg1, MalformedURLException arg2) {}

      @Override
      public void loadScriptError(HtmlPage arg0, URL arg1, Exception arg2) {}
    });
    webClient.setHTMLParserListener(new HTMLParserListener() {
      @Override
      public void warning(String message, URL url, String html, int line, int column, String key) {}

      @Override
      public void error(String message, URL url, String html, int line, int column, String key) {}
    });
    return webClient;
  }

  public static String classId(Node node) {
    if (node != null) {
      String className = node.attr("class");
      if (!CommonUtil.isEmpty(className)) {
        Matcher matcher = nodeMarker.matcher(className);
        if (matcher.find()) {
          return matcher.group(0);
        }
      }
    }
    return null;
  }

  public static Node fromCopy(final Node node, final Node parentCopy) {
    final String classId = classId(node);
    if (CommonUtil.isEmpty(classId)) {
      return null;
    }
    class MyVisitor implements NodeVisitor {
      Node found = null;

      @Override
      public void tail(Node n, int d) {}

      @Override
      public void head(Node n, int d) {
        if (classId.equals(classId(n))) {
          found = n;
        }
      }
    }
    MyVisitor visitor = new MyVisitor();
    parentCopy.traverse(visitor);
    return visitor.found;
  }

  public static WebElement toElement(RemoteWebDriver driver, Node node) {
    if (node == null) {
      return null;
    }
    try {
      String classId = classId(node);
      if (classId != null) {
        return driver.findElementByClassName(classId);
      }
    } catch (Throwable t) {
      Log.exception(t);
    }
    Log.warn("FAIL - could not convert Node to WebElement");
    return null;
  }

  public static void clean(List<Node> nodes) {
    for (Node node : nodes) {
      clean(node);
    }
  }

  private static final int MAX_HTML_CACHE = 10000;
  private static final Map<Node, String> htmlCache = new HashMap<Node, String>(MAX_HTML_CACHE);

  public static void clearOuterHtmlCache() {
    htmlCache.clear();
  }

  public static String outerHtml(Node node) {
    if (htmlCache.containsKey(node)) {
      return htmlCache.get(node);
    }
    String html = node.outerHtml();
    if (htmlCache.size() == MAX_HTML_CACHE) {
      htmlCache.clear();
    }
    htmlCache.put(node, html);
    return html;
  }

  public static String outerHtml(List<Node> nodes) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; nodes != null && i < nodes.size(); i++) {
      builder.append(nodes.get(i).outerHtml());
      if (i + 1 < nodes.size()) {
        builder.append("\n");
      }
    }
    return builder.toString().trim();
  }

  public static void clean(Node node) {
    node.traverse(new NodeVisitor() {
      @Override
      public void tail(Node node, int depth) {}

      @Override
      public void head(Node node, int depth) {
        String classAttr = node.attr("class");
        classAttr = cleanClass(classAttr);
        if (CommonUtil.isEmpty(classAttr)) {
          node.removeAttr("class");
        } else {
          node.attr("class", classAttr);
        }
      }
    });
  }

  public static String cleanClass(String classStr) {
    return nodeMarker.matcher(
        hiddenMarker.matcher(
            filteredMarker.matcher(
                filteredLenientMarker.matcher(
                    classStr).replaceAll("")).replaceAll("")).replaceAll("")).replaceAll("")
        .replaceAll("\\s+", " ").trim();
  }

  private static void markVisible(Node node) {
    if (node != null) {
      if (node.nodeName().equals("select")) {
        for (Node child : node.childNodes()) {
          child.attr("class", hiddenMarker.matcher(child.attr("class")).replaceAll(""));
        }
      }
      node.attr("class", hiddenMarker.matcher(node.attr("class")).replaceAll(""));
      markVisible(node.parent());
    }
  }

  private static void markFiltered(Node node, final boolean lenient) {
    if (lenient) {
      if (!isFilteredLenient(node)) {
        node.attr("class", node.attr("class") + " " + FILTERED_LENIENT_MARKER + " ");
      }
    } else {
      node.traverse(new NodeVisitor() {
        @Override
        public void tail(Node n, int d) {}

        @Override
        public void head(Node n, int d) {
          if (!isFiltered(n)) {
            n.attr("class", n.attr("class") + " " + FILTERED_MARKER + " ");
          }
        }
      });
    }
  }

  public static Element markTestElement(Element element) {
    element.traverse(new NodeVisitor() {
      @Override
      public void tail(Node node, int level) {}

      @Override
      public void head(Node node, int level) {
        node.attr("class", nodeMarker.matcher(node.attr("class")).replaceAll(""));
      }
    });
    element.traverse(new NodeVisitor() {
      int count = 0;

      @Override
      public void tail(Node node, int level) {}

      @Override
      public void head(Node node, int level) {
        ++count;
        node.attr("class", node.attr("class") + " " + NODE_MARKER + count + " ");
      }
    });
    return element;
  }

  public static Document clean(String string, String url) {
    Document doc = parse(string, url);
    clean(doc.childNodes());
    return doc;
  }

  private static Document parse(String string, String url) {
    String newString = string;
    try {
      StringWebResponse response = new StringWebResponse(newString,
          charset(newString), new URL(url));
      WebClient client = newWebClient();
      HtmlPage page = HTMLParser.parseHtml(response, client.getCurrentWindow());
      newString = page.asXml();
      newString = newString.replaceFirst("<[^>]+>", "");
      Document doc = CommonUtil.parse(newString, false);
      return doc;
    } catch (Throwable t) {
      Log.exception(t);
    }
    return Jsoup.parse(string);
  }

  public static Element openElement(final RemoteWebDriver driver, final String[] whitelist,
      final String[] patterns, final UrlTransform[] transforms) throws ActionFailed {
    try {
      driver.executeScript(
          "      var all = document.getElementsByTagName('*');"
              + "for(var i = 0; i < all.length; i++){"
              + "  if(all[i].className){"
              + "    all[i].className=all[i].className.replace(/"
              + NODE_MARKER + "\\d+/g,'').replace(/"
              + HIDDEN_MARKER + "/g,'').replace(/"
              + FILTERED_MARKER + "/g,'').replace(/"
              + FILTERED_LENIENT_MARKER + "/g,'').replace(/\\s+/g,' ').trim();"
              + "  }"
              + "}"
              + isVisible
              + "for(var j = 0; j < all.length; j++){"
              + "  all[j].className += ' " + NODE_MARKER + "'+j+' ';"
              + "  if(!isVisible(all[j])){"
              + "    all[j].className += ' " + HIDDEN_MARKER + " ';"
              + "  }"
              + "}");
      String url = driver.getCurrentUrl();
      new URL(url);
      Element element = parse(driver.getPageSource(), url).body();
      element.traverse(new NodeVisitor() {
        @Override
        public void tail(Node node, int depth) {}

        @Override
        public void head(Node node, int depth) {
          if (!node.nodeName().equals("#text") && !isEmpty(node)) {
            markVisible(node);
          }
        }
      });
      if ((whitelist != null && whitelist.length > 0)
          || (patterns != null && patterns.length > 0)) {
        element.traverse(new NodeVisitor() {
          @Override
          public void tail(Node node, int depth) {}

          @Override
          public void head(Node node, int depth) {
            if (node.nodeName().equals("a")) {
              if (isUrlFiltered(driver.getCurrentUrl(), node.attr("href"), whitelist, patterns, transforms)) {
                markFiltered(node, false);
              }
            } else {
              String urlAttr = Util.urlFromAttr(node);
              if (!CommonUtil.isEmpty(urlAttr)
                  && isUrlFiltered(driver.getCurrentUrl(), urlAttr, whitelist, patterns, transforms)) {
                markFiltered(node, true);
              }
            }
          }
        });
      }
      return element;
    } catch (Exception e) {
      Log.exception(e);
      throw new ActionFailed(e);
    }
  }

  public static boolean isResultFiltered(Result result, String[] whitelist, String[] patterns) {
    return isUrlFiltered(null, result.url(), whitelist, patterns, null);
  }

  private static boolean isUrlFiltered(String currentUrl, String url, String[] whitelist,
      String[] patterns, UrlTransform[] transforms) {
    if (!isUrlFilteredHelper(currentUrl, url, whitelist, patterns, transforms)) {
      return false;
    }
    try {
      List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), "UTF-8");
      for (NameValuePair pair : params) {
        String param = null;
        try {
          param = new URI(pair.getValue()).toString();
        } catch (Throwable t) {
          continue;
        }
        if (param != null && (param.startsWith("http:") || param.startsWith("https:"))
            && !isUrlFilteredHelper(
                currentUrl, param.toString(), whitelist, patterns, transforms)) {
          return false;
        }
      }
    } catch (Throwable t) {}
    return true;
  }

  private static boolean isUrlFilteredHelper(String currentUrl, String url, String[] whitelist,
      String[] patterns, UrlTransform[] transforms) {
    url = Util.transformUrl(url, transforms, false);
    if (!CommonUtil.isEmpty(url)) {
      if (!CommonUtil.isEmpty(currentUrl)) {
        url = toCleanUrl(currentUrl, url);
      }
      int max = Math.max(whitelist == null ? 0 : whitelist.length, patterns == null ? 0 : patterns.length);
      for (int i = 0; i < max; i++) {
        if (!CommonUtil.isEmpty(url)) {
          if (whitelist != null && i < whitelist.length &&
              url.toLowerCase().contains(whitelist[i].toLowerCase())) {
            return false;
          }
          if (patterns != null && i < patterns.length &&
              url.toLowerCase().matches(patterns[i].toLowerCase())) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public static int diff(Collection<String> collectionA, Collection<String> collectionB) {
    Collection<String> composite = new HashSet<String>();
    composite.addAll(collectionA);
    composite.addAll(collectionB);
    int diff = 0;
    for (String string : composite) {
      if (!collectionA.contains(string) || !collectionB.contains(string)) {
        diff++;
      }
    }
    return diff;
  }

  public static boolean isItem(String name) {
    for (int i = 0; i < items.length; i++) {
      if (name.equalsIgnoreCase(items[i])) {
        return true;
      }
    }
    return false;
  }

  public static boolean isBlock(String name) {
    for (int i = 0; i < blocks.length; i++) {
      if (name.equalsIgnoreCase(blocks[i])) {
        return true;
      }
    }
    return false;
  }

  private static boolean isProximityBlock(String name) {
    for (int i = 0; i < proximityBlocks.length; i++) {
      if (name.equalsIgnoreCase(proximityBlocks[i])) {
        return true;
      }
    }
    return false;
  }

  public static boolean isContent(Node node) {
    for (int i = 0; i < content.length; i++) {
      if (node.nodeName().equalsIgnoreCase(content[i])) {
        return true;
      }
    }
    return false;
  }

  public static boolean isUnbound(String name) {
    for (int i = 0; i < unbound.length; i++) {
      if (name.equalsIgnoreCase(unbound[i])) {
        return true;
      }
    }
    return false;
  }

  public static boolean isFormatting(String name) {
    for (int i = 0; i < formatting.length; i++) {
      if (name.equalsIgnoreCase(formatting[i])) {
        return true;
      }
    }
    return false;
  }

  public static boolean isDecoration(String name) {
    for (int i = 0; i < decoration.length; i++) {
      if (name.equalsIgnoreCase(decoration[i])) {
        return true;
      }
    }
    return false;
  }

  public static int nearestBlock(Node node) {
    int nearest = 0;
    Node parent = node.parent();
    while (parent != null) {
      ++nearest;
      if (isProximityBlock(parent.nodeName())) {
        return nearest;
      }
      parent = parent.parent();
    }
    return Integer.MAX_VALUE;
  }

  public static boolean contains(List<String> list, List<List<String>> listOfLists) {
    for (List<String> cur : listOfLists) {
      if (isSame(list, cur)) {
        return true;
      }
    }
    return false;
  }

  public static int dist(String str1, String str2) {
    if (distCache.size() > MAX_DIST_CACHE) {
      distCache.clear();
    }
    String cacheKey = str1 + "<<>>" + str2;
    if (distCache.containsKey(cacheKey)) {
      return distCache.get(cacheKey);
    }
    int dist = StringUtils.getLevenshteinDistance(str1, str2);
    distCache.put(cacheKey, dist);
    return dist;
  }

  public static boolean overlaps(List<Node> nodes, List<Node> targets) {
    final Collection<Node> all = new HashSet<Node>();
    for (Node target : targets) {
      target.traverse(new NodeVisitor() {
        @Override
        public void tail(Node n, int d) {}

        @Override
        public void head(Node n, int d) {
          all.add(n);
        }
      });
    }
    final boolean overlaps[] = new boolean[1];
    for (Node node : nodes) {
      node.traverse(new NodeVisitor() {
        @Override
        public void tail(Node n, int d) {}

        @Override
        public void head(Node n, int d) {
          if (!overlaps[0]) {
            if (all.contains(n)) {
              overlaps[0] = true;
            }
          }
        }
      });
      if (overlaps[0]) {
        return true;
      }
    }
    return false;
  }

  public static void trimLargeItems(int[] stringLengths, List<? extends Object> originals) {
    DescriptiveStatistics stats = new DescriptiveStatistics();
    for (int i = 0; i < stringLengths.length; i++) {
      stats.addValue(stringLengths[i]);
    }
    double stdDev = stats.getStandardDeviation();
    double mean = stats.getMean();
    List<Object> toRemove = new ArrayList<Object>();
    for (int i = 0; i < stringLengths.length; i++) {
      double diff = stringLengths[i] - mean;
      if (diff / stdDev > 4d) {
        toRemove.add(originals.get(i));
      }
    }
    for (Object obj : toRemove) {
      originals.remove(obj);
    }
  }

  public static void trimLargeNodes(List<Node> nodes) {
    int[] stringLengths = new int[nodes.size()];
    for (int i = 0; i < nodes.size(); i++) {
      stringLengths[i] = Util.outerHtml(nodes.get(i)).length();
    }
    trimLargeItems(stringLengths, nodes);
  }

  public static void trimLargeResults(List<Result> results) {
    int[] stringLengths = new int[results.size()];
    for (int i = 0; i < results.size(); i++) {
      stringLengths[i] = Util.outerHtml(results.get(i).getNodes()).length();
    }
    trimLargeItems(stringLengths, results);
  }

  public static boolean isSame(List<String> lhs, List<String> rhs) {
    if (lhs == null && rhs == null) {
      return true;
    }
    if (lhs == null) {
      return false;
    }
    if (rhs == null) {
      return false;
    }
    if (lhs.size() != rhs.size()) {
      return false;
    }
    for (int i = 0; i < lhs.size(); i++) {
      if (!lhs.get(i).equals(rhs.get(i))) {
        return false;
      }
    }
    return true;
  }

  public static List<String> join(List<String> list1, List<String> list2) {
    List<String> list = new ArrayList<String>();
    for (String cur : list1) {
      if (!list.contains(cur)) {
        list.add(cur);
      }
    }
    for (String cur : list2) {
      if (!list.contains(cur)) {
        list.add(cur);
      }
    }
    return list;
  }

  public static boolean isHidden(Node node) {
    return node.attr("class").indexOf(HIDDEN_MARKER) > -1;
  }

  public static boolean isFiltered(Node node) {
    return node.attr("class").indexOf(FILTERED_MARKER) > -1;
  }

  public static boolean isFilteredLenient(Node node) {
    return node.attr("class").indexOf(FILTERED_MARKER) > -1
        || node.attr("class").indexOf(FILTERED_LENIENT_MARKER) > -1;
  }

  public static boolean isEmpty(Node node) {
    return isEmpty(node, true);
  }

  public static boolean isEmpty(Node node, boolean doFilter) {
    return node == null
        || node.nodeName().equals("#comment")
        || node.nodeName().equals("#data")
        || node.nodeName().equals("style")
        || node.nodeName().equals("script")
        || isHidden(node)
        || (doFilter && isFiltered(node))
        || (node.nodeName().equals("#text")
        && CommonUtil.isEmpty(node.toString(), true));
  }

  private static boolean click(RemoteWebDriver driver, WebElement toClick, boolean shift) {
    try {
      Actions action = new Actions(driver);
      driverSleepVeryShort();
      action.moveToElement(toClick).perform();
      if (shift) {
        driver.getKeyboard().pressKey(Keys.SHIFT);
      }
      toClick.click();
      if (shift) {
        driver.getKeyboard().releaseKey(Keys.SHIFT);
      }
      Util.driverSleepVeryShort();
    } catch (Throwable t) {
      return false;
    }
    return true;
  }

  public static boolean click(RemoteWebDriver driver, WebElement toClick) {
    return click(driver, toClick, false);
  }

  public static boolean clickToNewWindow(RemoteWebDriver driver, WebElement toClick) {
    return click(driver, toClick, true);
  }

  public static boolean doClicks(RemoteWebDriver driver, HtmlNode[] controls, Element body) throws ActionFailed {
    if (WebApp.DEBUG) {
      System.out.println("Doing clicks");
    }
    boolean clicked = false;
    if (controls != null && controls.length > 0) {

      if (body == null) {
        body = Util.openElement(driver, null, null, null);
      }
      for (int i = 0; i < controls.length; i++) {
        WebElement element = Util.toElement(driver, controls[i], body);
        if (element != null) {
          clicked = true;
          click(driver, element);
          if (controls[i].longRequest) {
            Util.driverSleepLong();
          }
        }
      }
    }
    return clicked;
  }

  public static WebElement toElement(RemoteWebDriver driver, HtmlNode htmlNode, Element body) throws ActionFailed {
    if (body == null) {
      body = Util.openElement(driver, null, null, null);
    }
    if (!CommonUtil.isEmpty(htmlNode.id)) {
      WebElement element = toElement(driver, body.getElementById(htmlNode.id));
      if (element != null) {
        return element;
      }
    }
    List<Elements> selected = new ArrayList<Elements>();
    if (!CommonUtil.isEmpty(htmlNode.tagName)) {
      selected.add(body.getElementsByTag(htmlNode.tagName));
    } else if (!CommonUtil.isEmpty(htmlNode.href)) {
      selected.add(body.getElementsByTag("a"));
    }
    if (!CommonUtil.isEmpty(htmlNode.name)) {
      selected.add(body.getElementsByAttributeValue("name", htmlNode.name));
    }
    if (!CommonUtil.isEmpty(htmlNode.type)) {
      selected.add(body.getElementsByAttributeValue("type", htmlNode.type));
    }
    if (!CommonUtil.isEmpty(htmlNode.value)) {
      selected.add(body.getElementsByAttributeValue("value", htmlNode.value));
    }
    if (!CommonUtil.isEmpty(htmlNode.title)) {
      selected.add(body.getElementsByAttributeValue("title", htmlNode.title));
    }
    if (htmlNode.classes != null && htmlNode.classes.length > 0) {
      Map<Element, Integer> found = new HashMap<Element, Integer>();
      for (int i = 0; i < htmlNode.classes.length; i++) {
        Elements elements = body.getElementsByClass(htmlNode.classes[i]);
        for (Element element : elements) {
          if (!found.containsKey(element)) {
            found.put(element, 0);
          }
          found.put(element, found.get(element) + 1);
        }
      }
      Elements elements = new Elements();
      for (int i = htmlNode.classes.length; i > 0; i--) {
        for (Map.Entry<Element, Integer> entry : found.entrySet()) {
          if (entry.getValue() == i) {
            elements.add(entry.getKey());
          }
        }
        if (!elements.isEmpty()) {
          break;
        }
      }
      selected.add(elements);
    }
    if (!CommonUtil.isEmpty(htmlNode.href)) {
      Elements hrefs = body.getElementsByAttribute("href");
      Elements toAdd = new Elements();
      String currentUrl = driver.getCurrentUrl();
      String hrefGiven = htmlNode.href;
      for (Element href : hrefs) {
        String hrefFound = href.attr("href");
        if (hrefGiven.equalsIgnoreCase(hrefFound)) {
          toAdd.add(href);
        } else {
          String uriGiven = Util.toCanonicalUri(currentUrl, hrefGiven);
          String uriFound = Util.toCanonicalUri(currentUrl, hrefFound);
          if (uriGiven.equalsIgnoreCase(uriFound)) {
            toAdd.add(href);
          }
        }
      }
      selected.add(toAdd);
    }
    if (!CommonUtil.isEmpty(htmlNode.innerText)) {
      selected.add(body.getElementsMatchingText(Pattern.quote(htmlNode.innerText)));
    }
    if (htmlNode.multiple != null) {
      selected.add(body.getElementsByAttribute("multiple"));
    }
    Map<Element, Integer> votes = new HashMap<Element, Integer>();
    for (Elements elements : selected) {
      for (Element element : elements) {
        if (!Util.isHidden(element)) {
          if (!votes.containsKey(element)) {
            votes.put(element, 0);
          }
          votes.put(element, votes.get(element) + 1);
        }
      }
    }
    int maxVote = 0;
    Element maxElement = null;
    for (Map.Entry<Element, Integer> entry : votes.entrySet()) {
      if (entry.getValue() > maxVote) {
        maxVote = entry.getValue();
        maxElement = entry.getKey();
      }
    }
    return toElement(driver, maxElement);
  }

  private static String toCleanUrl(String fullUrl, String href) {
    String clean = toCanonicalUri(fullUrl, href);
    return clean.startsWith("//") ? (fullUrl.startsWith("https:") ? "https:" + clean : "http:" + clean) : clean;
  }

  private static String toCanonicalUri(String fullUrl, String href) {
    if (href.startsWith("/") && !href.startsWith("//")) {
      return Util.getUriBase(fullUrl, false) + href;
    }
    if (!uriScheme.matcher(href).matches() && !href.startsWith("/")) {
      return Util.getUriBase(fullUrl, true) + href;
    }
    if (!uriScheme.matcher(href).matches() && !href.startsWith("//")) {
      return "//" + href.replaceAll(Util.schemeFragment, "");
    }

    return href;
  }

  private static String getUriBase(String fullUri, boolean relative) {
    if (fullUri.contains("?")) {
      fullUri = fullUri.split("\\?")[0];
    }
    if (fullUri.contains("#")) {
      fullUri = fullUri.split("#")[0];
    }
    if (fullUri.contains("&")) {
      fullUri = fullUri.split("&")[0];
    }
    if (fullUri.endsWith("/")) {
      fullUri = fullUri.substring(0, fullUri.length() - 1);
    }
    if (relative) {
      String relativeBase = "";
      int index = fullUri.lastIndexOf('/');
      int doubleSlashIndex = fullUri.indexOf("//");
      if (index > doubleSlashIndex + 1) {
        relativeBase = fullUri.substring(0, index + 1);
      } else {
        relativeBase = fullUri;
      }
      relativeBase = relativeBase.replaceAll(schemeFragment, "");
      relativeBase = "//" + relativeBase;
      if (!relativeBase.endsWith("/")) {
        relativeBase = relativeBase + "/";
      }
      return relativeBase;
    }
    String absoluteBase = "";
    int start = 0;
    int doubleSlash = fullUri.indexOf("//");
    if (doubleSlash > -1) {
      start = doubleSlash + "//".length();
    }
    int index = fullUri.indexOf('/', start);
    if (index > -1) {
      absoluteBase = fullUri.substring(0, index);
    } else {
      absoluteBase = fullUri;
    }
    absoluteBase = absoluteBase.replaceAll(schemeFragment, "");
    absoluteBase = "//" + absoluteBase;
    return absoluteBase;
  }

  public static List<Result> fixUrls(List<Result> results, String currentUrl) {
    if (results == null) {
      return null;
    }
    List<String> urls = new ArrayList<String>();
    for (Result result : results) {
      urls.add(result.url());
    }
    urls = fixUrlStrings(urls, currentUrl);
    for (int i = 0; i < results.size(); i++) {
      results.get(i).tweakUrl(urls.get(i));
    }
    return results;
  }

  public static String fixUrlString(String url, String currentUrl) {
    if (CommonUtil.isEmpty(url)) {
      return url;
    }
    List<String> urls = new ArrayList<String>();
    urls.add(url);
    return fixUrlStrings(urls, currentUrl).get(0);
  }

  public static List<String> fixUrlStrings(List<String> urls, String currentUrl) {
    if (urls == null) {
      return null;
    }
    if (currentUrl == null) {
      return urls;
    }
    String absolute = new String(currentUrl);
    int slash = absolute.lastIndexOf('/');
    if (slash == -1) {
      return urls;
    }
    int query = absolute.indexOf('?');
    if (query > -1) {
      absolute = absolute.substring(0, query);
    }
    int hash = absolute.indexOf('#');
    if (hash > -1) {
      absolute = absolute.substring(0, hash);
    }
    int amp = absolute.indexOf('&');
    if (amp > -1) {
      absolute = absolute.substring(0, amp);
    }
    if (absolute.charAt(absolute.length() - 1) != '/') {
      absolute = absolute + "/";
    }
    String relative = new String(absolute);
    absolute = absolute.substring(0, absolute.lastIndexOf('/'));
    int absIndex = absolute.lastIndexOf('/');
    if (absIndex > -1 && absIndex - 2 > -1 && absolute.charAt(absIndex - 2) != ':') {
      absolute = absolute.substring(0, absIndex);
    }
    absolute = absolute + "/";
    List<String> newUrls = new ArrayList<String>();
    for (String url : urls) {
      String newUrl;
      if (CommonUtil.isEmpty(url)) {
        newUrl = url;
      } else if (url.startsWith("//")) {
        newUrl = (currentUrl.startsWith("https:") ? "https:" : "http:") + url;
      } else if (url.startsWith("http://localhost/")) {
        newUrl = absolute +
            url.substring("http://localhost/".length() - 1, url.length());
      } else if (url.startsWith("/")) {
        newUrl = absolute + url.substring(1);
      } else if (!url.contains("://")) {
        newUrl = relative + url;
      } else {
        newUrl = url;
      }
      newUrls.add(newUrl);
    }
    if (WebApp.DEBUG) {
      for (String url : urls) {
        System.out.println("result: " + url);
      }
    }
    return newUrls;
  }

  public static String transformUrl(String url, UrlTransform[] urlTransforms, boolean forExport) {
    List<String> urls = new ArrayList<String>();
    urls.add(url);
    return transformUrlStrings(urls, urlTransforms, forExport).get(0);
  }

  public static List<Result> transformUrls(List<Result> results, UrlTransform[] urlTransforms, boolean forExport) {
    if (results == null) {
      return null;
    }
    List<String> urls = new ArrayList<String>();
    for (Result result : results) {
      urls.add(result.url());
    }
    urls = transformUrlStrings(urls, urlTransforms, forExport);
    for (int i = 0; urls != null && i < results.size(); i++) {
      results.get(i).tweakUrl(urls.get(i));
    }
    return results;
  }

  public static List<String> transformUrlStrings(List<String> urls, UrlTransform[] urlTransforms, boolean forExport) {
    List<String> newUrls = new ArrayList<String>();
    if (urlTransforms != null && urlTransforms.length != 0 && urls != null) {
      for (String url : urls) {
        String newUrl = url;
        for (int i = 0; urlTransforms != null && i < urlTransforms.length; i++) {
          if (!CommonUtil.isEmpty(urlTransforms[i].regex)
              && newUrl != null
              && urlTransforms[i] != null
              && ((forExport && urlTransforms[i].transformForExportOnly)
              || (!forExport && !urlTransforms[i].transformForExportOnly))) {
            Pattern pattern = Pattern.compile(urlTransforms[i].regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
            Matcher matcher = pattern.matcher(newUrl);
            if (matcher.find()) {
              if (urlTransforms[i].replaceAll) {
                if (urlTransforms[i].replaceAllRecursive) {
                  String transformed = matcher.replaceAll(urlTransforms[i].replacement);
                  String transformedRec = pattern.matcher(transformed).replaceAll(urlTransforms[i].replacement);
                  while (!transformed.equals(transformedRec)) {
                    transformed = transformedRec;
                    transformedRec = pattern.matcher(transformedRec).replaceAll(urlTransforms[i].replacement);
                  }
                  newUrl = transformed;
                } else {
                  newUrl = matcher.replaceAll(urlTransforms[i].replacement);
                }
              } else {
                newUrl = matcher.replaceFirst(urlTransforms[i].replacement);
              }
              if (!urlTransforms[i].multipleTransforms) {
                break;
              }
            }
          }
        }
        newUrls.add(newUrl);
      }
    } else {
      return urls;
    }
    return newUrls;
  }
}

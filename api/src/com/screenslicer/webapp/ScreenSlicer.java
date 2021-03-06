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
package com.screenslicer.webapp;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.screenslicer.api.datatype.Contact;
import com.screenslicer.api.datatype.HtmlNode;
import com.screenslicer.api.datatype.SearchResult;
import com.screenslicer.api.request.Cancel;
import com.screenslicer.api.request.EmailExport;
import com.screenslicer.api.request.Extract;
import com.screenslicer.api.request.Fetch;
import com.screenslicer.api.request.FormLoad;
import com.screenslicer.api.request.FormQuery;
import com.screenslicer.api.request.KeywordQuery;
import com.screenslicer.api.request.Request;
import com.screenslicer.common.CommonUtil;
import com.screenslicer.common.Log;
import com.screenslicer.webapp.WebApp.Callback;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.emory.mathcs.backport.java.util.Collections;

public final class ScreenSlicer {
  public static interface CustomApp {
    Map<String, Object> configure(Request request, Map<String, Object> args);

    Map<String, List<List<String>>> tableData(Request request, Map<String, Object> args);

    Map<String, byte[]> binaryData(Request request, Map<String, Object> args);

    Map<String, Map<String, Object>> jsonData(Request request, Map<String, Object> args);
  }

  private static SecureRandom rand = new SecureRandom();
  public static final List<SearchResult> NULL_RESULTS = Collections.unmodifiableList(Arrays.asList(new SearchResult[0]));
  public static final List<HtmlNode> NULL_CONTROLS = Collections.unmodifiableList(Arrays.asList(new HtmlNode[0]));
  public static final Contact NULL_CONTACT = new Contact();
  public static final String NULL_FETCH = "";

  private static final Contact nullContact() {
    for (Field field : Contact.class.getFields()) {
      try {
        field.set(NULL_CONTACT, null);
      } catch (Throwable t) {}
    }
    return NULL_CONTACT;
  }

  public static final synchronized void startCustomApp(final ScreenSlicer.CustomApp customApp) {
    WebApp.start("custom-app", false, 9000, true, null, new Callback() {
      @Override
      public void call() {
        ScreenSlicerClient.init(customApp);
      }
    });
  }

  public static final boolean isCancelled(String runGuid) {
    return ScreenSlicerDriver.isCancelled(runGuid)
        || ScreenSlicerClient.isCancelled(runGuid);
  }

  public static final void cancel(Cancel args) {
    try {
      for (int i = 0; i < args.instances.length; i++) {
        Request req = new Request();
        req.runGuid = args.runGuid;
        CommonUtil.post("http://" + args.instances[i] + ":8888/core-batch/cancel",
            args.instances[i], CommonUtil.gson.toJson(req, CommonUtil.objectType));
      }
    } catch (Throwable t) {
      Log.exception(t);
    }
  }

  public static final boolean isBusy(String instanceIp) {
    try {
      return CommonUtil.post("http://" + instanceIp + ":8888/core-batch/busy",
          instanceIp, "") != CommonUtil.NOT_BUSY;
    } catch (Throwable t) {
      Log.exception(t);
    }
    return true;
  }

  public static final void export(Request request, EmailExport args) {
    try {
      String instance = instanceIp(request);
      CommonUtil.post("http://" + instance + ":8888/core-batch/export-email",
          instance, CommonUtil.combinedJson(request, args));
    } catch (Throwable t) {
      Log.exception(t);
    }
  }

  public static final List<SearchResult> queryForm(Request request, FormQuery args) {
    try {
      String instance = instanceIp(request);
      List<SearchResult> ret = CommonUtil.gson.fromJson(
          CommonUtil.post("http://" + instance + ":8888/core-batch/query-form",
              instance, CommonUtil.combinedJson(request, args)),
          new TypeToken<List<SearchResult>>() {}.getType());
      return ret == null ? NULL_RESULTS : ret;
    } catch (Throwable t) {
      Log.exception(t);
    }
    return NULL_RESULTS;
  }

  public static final List<SearchResult> queryKeyword(Request request, KeywordQuery args) {
    try {
      String instance = instanceIp(request);
      List<SearchResult> ret = CommonUtil.gson.fromJson(CommonUtil.post(
          "http://" + instance + ":8888/core-batch/query-keyword",
          instance, CommonUtil.combinedJson(request, args)),
          new TypeToken<List<SearchResult>>() {}.getType());
      return ret == null ? NULL_RESULTS : ret;
    } catch (Throwable t) {
      Log.exception(t);
    }
    return NULL_RESULTS;
  }

  public static final List<HtmlNode> loadForm(Request request, FormLoad args) {
    try {
      String instance = instanceIp(request);
      List<HtmlNode> ret = CommonUtil.gson.fromJson(CommonUtil.post(
          "http://" + instance + ":8888/core-batch/load-form",
          instance, CommonUtil.combinedJson(request, args)),
          new TypeToken<List<HtmlNode>>() {}.getType());
      return ret == null ? NULL_CONTROLS : ret;
    } catch (Throwable t) {
      Log.exception(t);
    }
    return NULL_CONTROLS;
  }

  //TODO support multiple people (return a list)
  public static final Contact extractPerson(Request request, Extract args) {
    try {
      String instance = instanceIp(request);
      Contact ret = CommonUtil.gson.fromJson(CommonUtil.post(
          "http://" + instance + ":8888/core-batch/extract-person",
          instance, CommonUtil.combinedJson(request, args)),
          new TypeToken<Contact>() {}.getType());
      return ret == null ? nullContact() : ret;
    } catch (Throwable t) {
      Log.exception(t);
    }
    return nullContact();
  }

  public static final String fetch(Request request, Fetch args) {
    try {
      String instance = instanceIp(request);
      SearchResult result = CommonUtil.gson.fromJson(
          CommonUtil.post("http://" + instance + ":8888/core-batch/fetch",
              instance, CommonUtil.combinedJson(request, args)),
          new TypeToken<SearchResult>() {}.getType());
      return result == null ? NULL_FETCH : (result.pageHtml == null ? NULL_FETCH : result.pageHtml);
    } catch (Throwable t) {
      Log.exception(t);
    }
    return NULL_FETCH;
  }

  private static final String instanceIp(Request request) {
    return request.instances[rand.nextInt(request.instances.length)];
  }
}

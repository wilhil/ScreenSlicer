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
package com.screenslicer.api.datatype;

import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.screenslicer.common.CommonUtil;
import com.screenslicer.common.Random;

public final class HtmlNode {
  public static final HtmlNode instance(String json) {
    return instance((Map<String, Object>) CommonUtil.gson.fromJson(json, CommonUtil.objectType));
  }

  public static final List<HtmlNode> instances(String json) {
    return instances((Map<String, Object>) CommonUtil.gson.fromJson(json, CommonUtil.objectType));
  }

  public static final HtmlNode instance(Map<String, Object> args) {
    return CommonUtil.constructFromMap(HtmlNode.class, args);
  }

  public static final List<HtmlNode> instances(Map<String, Object> args) {
    return CommonUtil.constructListFromMap(HtmlNode.class, args);
  }

  public static final String toJson(HtmlNode obj) {
    return CommonUtil.gson.toJson(obj, new TypeToken<HtmlNode>() {}.getType());
  }

  public static final String toJson(HtmlNode[] obj) {
    return CommonUtil.gson.toJson(obj, new TypeToken<HtmlNode[]>() {}.getType());
  }

  public static final String toJson(List<HtmlNode> obj) {
    return CommonUtil.gson.toJson(obj, new TypeToken<List<HtmlNode>>() {}.getType());
  }

  /**
   * Whether actions on this node should be followed by a long delay.
   * Useful for AJAX apps, such as with a search button that does an asynchonous
   * post.
   */
  public boolean longRequest = false;

  /**
   * HTML tag name (e.g., select, button, form, a)
   */
  public String tagName;
  /**
   * HTML element ID attribute
   */
  public String id;
  /**
   * HTML element name attribute
   */
  public String name;
  /**
   * HTML label value
   */
  public String label;

  /**
   * HTML element type attribute (e.g., "text" for &lt;input type="text"&gt;)
   */
  public String type;
  /**
   * HTML element value attribute
   */
  public String value;
  /**
   * HTML element title attribute
   */
  public String title;
  /**
   * HTML element HREF attribute (e.g., HREFs on an anchor)
   */
  public String href;
  /**
   * CSS class names listed in the class attribute of an HTML element
   */
  public String[] classes;

  /**
   * Text contained in an HTML node
   */
  public String innerText;
  /**
   * HTML contained in an HTML node
   */
  public String innerHtml;

  /**
   * Whether mutli-select is enabled (only valid for checkboxes and selects)
   */
  public String multiple;
  /**
   * Values of the element
   */
  public String[] optionValues;
  /**
   * Labels of the element
   */
  public String[] optionLabels;

  /**
   * GUID (a random string) for this node
   */
  public String guid = Random.next();
  /**
   * GUID which can be shared (like how multiple elements can have the same
   * name)
   */
  public String guidName = guid;
}

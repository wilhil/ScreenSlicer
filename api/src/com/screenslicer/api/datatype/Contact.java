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

public final class Contact {
  public static final Contact instance(String json) {
    return instance((Map<String, Object>) CommonUtil.gson.fromJson(json, CommonUtil.objectType));
  }

  public static final List<Contact> instances(String json) {
    return instances((Map<String, Object>) CommonUtil.gson.fromJson(json, CommonUtil.objectType));
  }

  public static final Contact instance(Map<String, Object> args) {
    return CommonUtil.constructFromMap(Contact.class, args);
  }

  public static final List<Contact> instances(Map<String, Object> args) {
    return CommonUtil.constructListFromMap(Contact.class, args);
  }

  public static final String toJson(Contact obj) {
    return CommonUtil.gson.toJson(obj, new TypeToken<Contact>() {}.getType());
  }

  public static final String toJson(Contact[] obj) {
    return CommonUtil.gson.toJson(obj, new TypeToken<Contact[]>() {}.getType());
  }

  public static final String toJson(List<Contact> obj) {
    return CommonUtil.gson.toJson(obj, new TypeToken<List<Contact>>() {}.getType());
  }

  /**
   * Name of a person
   */
  public String name;
  /**
   * Email for a person
   */
  public String email;
  /**
   * Phone number for a person
   */
  public String phone;
}

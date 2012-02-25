/*  
 * Copyright 2010-2012 Andrew Brock
 * 
 * This file is part of SkeetStalker.
 *
 * SkeetStalker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SkeetStalker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SkeetStalker.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catchwa.skeetstalker.shared;

public class Constants
{
  public static final long MILLISECONDS_IN_A_YEAR = 31556926000l;
  public static final long MILLISECONDS_IN_A_MONTH = 2629743830l;
  public static final long MILLISECONDS_IN_A_WEEK = 604800000l;
  public static final long MILLISECONDS_IN_A_DAY = 86400000l;
  public static final long MILLISECONDS_IN_A_HOUR = 3600000l;
  public static final long MILLISECONDS_IN_A_MINUTE = 60000l;
  public static final long MILLISECONDS_IN_A_SECOND = 1000l;
  
  public static final int CLIENT_TABLE_ROW_LIMIT = 15;
  public static final int CLIENT_QUESTIONS_TABLE_REFRESH_INTERVAL = (int) (5 * MILLISECONDS_IN_A_MINUTE);
  public static final int CLIENT_LAST_ONLINE_REFRESH_INTERVAL = (int)(5 * MILLISECONDS_IN_A_MINUTE);
  public static final String API_KEY = "EtpbakgUwEm-J18FYlsC2g";
  public static final int DEFAULT_ID = 22656;
  public static final String DEFAULT_SITE = "http://api.stackoverflow.com";
  public static final long SERVER_SITE_LIST_UPDATE_INTERVAL = MILLISECONDS_IN_A_DAY;
  public static final long SERVER_USER_TAG_REFRESH_INTERVAL = MILLISECONDS_IN_A_DAY;
  public static final long SERVER_UNANSWERED_QUESTION_REFRESH_INTERVAL = (int)(5 * MILLISECONDS_IN_A_MINUTE);
  public static final int SERVER_USERNAMES_TO_RETURN = 10;
}

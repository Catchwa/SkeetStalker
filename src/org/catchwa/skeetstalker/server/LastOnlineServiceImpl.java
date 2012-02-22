/*  
 * Copyright 2010 Andrew Brock
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

package org.catchwa.skeetstalker.server;

import net.sf.stackwrap4j.StackWrapper;
import net.sf.stackwrap4j.utils.StackUtils;

import org.catchwa.skeetstalker.client.LastOnlineService;
import org.catchwa.skeetstalker.server.schema.Stalkee;
import org.catchwa.skeetstalker.shared.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class LastOnlineServiceImpl extends RemoteServiceServlet implements LastOnlineService
{
  public String getLastOnline(int id, String site)
  {  
    Stalkee u = Utils.getCachedUser(id, site);
    if(u == null)
    {
      u = Utils.createUser(id, site);
      if(u == null)
      {
        return "User does not exist";
      }
    }
    StackWrapper sw = new StackWrapper(site, Constants.API_KEY);
    try
    {
      return "<a href=\"http://"+site+"/users/"+id+"\" target=\"_blank\">"+u.getName()+"</a> was last active "+StackUtils.formatElapsedTime(sw.getUserById(u.getId()).getLastAccessDate())+" ago";
    } catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
}

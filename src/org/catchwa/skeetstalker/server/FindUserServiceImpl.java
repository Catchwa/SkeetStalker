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

import java.util.List;

import net.sf.stackwrap4j.StackWrapper;
import net.sf.stackwrap4j.entities.User;
import net.sf.stackwrap4j.query.UserQuery;

import org.catchwa.skeetstalker.client.FindUserService;
import org.catchwa.skeetstalker.shared.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FindUserServiceImpl extends RemoteServiceServlet implements FindUserService
{
  @Override
  public String[] getUsers(String name, String site)
  {
    StackWrapper sw = new StackWrapper(site, Constants.API_KEY);
    UserQuery uq = new UserQuery();
    uq.setFilter(name);
    uq.setPageSize(20);
    List<User> users;
    try
    {
      users = sw.listUsers(uq);
    } catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
    
    String[] result = new String[Math.min(org.catchwa.skeetstalker.shared.Constants.SERVER_USERNAMES_TO_RETURN, users.size())];
    for(int i = 0; i < result.length; i++)
    {
      result[i] = users.get(i).getDisplayName()+" "+users.get(i).getId();
    }
    return result;
  }
}

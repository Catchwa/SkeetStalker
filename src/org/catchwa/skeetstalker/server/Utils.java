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

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import net.sf.stackwrap4j.StackWrapper;
import net.sf.stackwrap4j.entities.Tag;

import org.catchwa.skeetstalker.server.schema.Stalkee;
import org.catchwa.skeetstalker.shared.Constants;

public class Utils
{
  public static Stalkee createUser(int id, String site)
  {  
    StackWrapper sw = new StackWrapper(site, Constants.API_KEY);
    net.sf.stackwrap4j.entities.User user;
    List<Tag> tags;
    try
    {
      user = sw.getUserById(id);
      tags = user.getTags();
    } catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
    
    Stalkee u = new Stalkee(user.getId(), site, user.getDisplayName(), tagsListToStringList(tags), System.currentTimeMillis());
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try
    {
      pm.makePersistent(u);
    }
    finally
    {
      pm.close();
    }
    return u;
  }
  
  @SuppressWarnings("unchecked")
  public static Stalkee getCachedUser(long id, String site)
  {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String targetKey = id+"@"+site;
    Stalkee u = null;
    
    Query query = pm.newQuery(Stalkee.class);
    query.setFilter("key == targetKey");
    query.declareParameters("String targetKey");
    try
    {
      List<Stalkee> results = (List<Stalkee>) query.execute(targetKey);
      if(results.size() != 0)
      {
        u = results.get(0);
      }
    }
    finally
    {
      query.closeAll();
      pm.close();
    }
    return u;
  }
  
  public static void removeUser(long id, String site)
  {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String targetKey = id+"@"+site;
    
    Query query = pm.newQuery(Stalkee.class);
    query.setFilter("key == targetKey");
    query.declareParameters("String targetKey");
    try
    {
      query.deletePersistentAll(targetKey);
    }
    finally
    {
      query.closeAll();
      pm.close();
    }
  }
  
  private static List<String> tagsListToStringList(List<Tag> tags)
  {
    List<String> result = new ArrayList<String>(tags.size());
    for(int i = 0; i < tags.size(); i++)
    {
      result.add(tags.get(i).getName());
    }
    return result;
  }
}

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

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import net.sf.stackwrap4j.stackauth.StackAuth;
import net.sf.stackwrap4j.stackauth.entities.Site;

import org.catchwa.skeetstalker.client.StackSitesService;
import org.catchwa.skeetstalker.server.schema.StackServer;
import org.catchwa.skeetstalker.shared.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class StackSitesServiceImpl extends RemoteServiceServlet implements StackSitesService
{
  @Override
  public String[] getSites()
  {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery();
    query = pm.newQuery(StackServer.class);
    query.setOrdering("lastUpdated asc");
    List<StackServer> results = (List<StackServer>) query.execute();
    if(results.size() > 0 && System.currentTimeMillis() - results.get(0).getLastUpdated() < Constants.SERVER_SITE_LIST_UPDATE_INTERVAL)
    {
      String[] result = new String[results.size()];
      for(int i = 0; i < result.length; i++)
      {
        result[i] = results.get(i).getUrl()+"!"+results.get(i).getName();
      }
      return result;
    }
    else
    {
      try
      {
        List<Site> sites = StackAuth.getAllSites();
        String[] result = new String[sites.size()];
        for(int i = 0; i < result.length; i++)
        {
          StackServer ss = new StackServer(sites.get(i).getName(), sites.get(i).getApiEndpoint());
          pm.makePersistent(ss);
          result[i] = sites.get(i).getApiEndpoint()+"!"+sites.get(i).getName();
        }
        return result;
      } catch(Exception e)
      {
        e.printStackTrace();
      }
      return null;
    }
  }
}
